package edu.kit.satviz.network;

import edu.kit.satviz.serial.SerializationException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ConnectionManager {
  private enum State {
    NEW,
    STARTED,
    OPEN,
    FAILED,
    FINISHING,
    FINISHED
  }

  private final Object syncState = new Object();
  private volatile State state;

  private final InetSocketAddress serverAddress;
  private ServerSocketChannel serverChan;
  private Selector select;
  private final List<ConnectionContext> contexts;
  private final NetworkBlueprint bp;

  private Consumer<ConnectionId> lsAccept;
  private Consumer<String> lsFail;

  public ConnectionManager(int port, NetworkBlueprint bp) {
    serverAddress = new InetSocketAddress("localhost", port);
    this.contexts = new ArrayList<>();
    this.bp = bp;
  }

  private void terminateGlobal(boolean abnormal) {
    synchronized (syncState) {
      if (state == State.FINISHED || state == State.FAILED) {
        return;
      }

      if (state == State.OPEN || state == State.FINISHING) {
        try {
          serverChan.close();
          select.close();
        } catch (IOException e) {
          // not our problem
        }
        for (ConnectionContext ctx : contexts) {
          ctx.close(abnormal);
        }
      }
      state = abnormal ? State.FAILED : State.FINISHED;
      if (state == State.FAILED) {
        lsFail.accept("fail");
      }
    }
  }

  private void doStart() {
    synchronized (syncState) {
      if (state != State.STARTED) { // someone already terminated
        return;
      }
      try {
        serverChan = ServerSocketChannel.open();
        serverChan.bind(serverAddress);
        serverChan.configureBlocking(false);
        select = Selector.open();
        serverChan.register(select, SelectionKey.OP_ACCEPT);
      } catch (IOException e) {
        terminateGlobal(true);
        return;
      }
      state = State.OPEN;
    }

    while (state == State.OPEN) {
      try {
        select.select();
      } catch (IOException e) {
        terminateGlobal(true);
        return;
      }
      Iterator<SelectionKey> iter = select.selectedKeys().iterator();
      while (iter.hasNext()) {
        SelectionKey key = iter.next();
        if (key.isAcceptable()) {
          ConnectionContext newCtx = null;
          try {
            SocketChannel newChan = serverChan.accept();
            newChan.register(select, SelectionKey.OP_READ);
            newChan.configureBlocking(false);
            newCtx = new ConnectionContext(
                new ConnectionId((InetSocketAddress) newChan.getRemoteAddress()),
                newChan,
                new Receiver(bp::getBuilder),
                null
            );
            contexts.add(newCtx);
          } catch (IOException e) {
            // we take this as a fatal condition
            terminateGlobal(true);
            return;
          }
          if (lsAccept != null) {
            lsAccept.accept(newCtx.getCid());
          }
        }
        if (key.isReadable()) {
          ((SocketChannel) key.channel()).read(null);
        }
        iter.remove();
      }
    }

    terminateGlobal(false);
  }

  public boolean start() {
    synchronized (syncState) {
      if (state != State.NEW) {
        return false;
      }
      state = State.STARTED;
    }

    new Thread(this::doStart).start();
    return true;
  }

  public void stop() {
    synchronized (syncState) {
      state = State.FINISHING;
    }
  }

  List<ConnectionId> getConnections() {
    return contexts.stream().map(ConnectionContext::getCid).toList();
  }

  void registerAccept(Consumer<ConnectionId> ls) {
    this.lsAccept = ls;
  }

  void registerGlobalFail(Consumer<String> ls) {
    this.lsFail = ls;
  }

  void register(ConnectionId cid, BiConsumer<ConnectionId, NetworkMessage> ls) {
    ConnectionContext ctx = getContextFrom(cid);
    if (ctx != null) {
      ctx.setListener(ls);
    }
  }

  private ConnectionContext getContextFrom(ConnectionId cid) {
    for (ConnectionContext ctx : contexts) {
      if (ctx.getCid() == cid) {
        return ctx;
      }
    }
    return null;
  }

  public void send(ConnectionId cid, Byte type, Object obj) throws IOException {
    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    ConnectionContext ctx = getContextFrom(cid);
    if (ctx == null) {
      throw new IOException("invalid connection ID");
    }

    byteOut.write(type);
    try {
      bp.serialize(type, obj, byteOut);
    } catch (SerializationException | ClassCastException e) {
      // fail this connection, but not the whole manager
      ctx.close(true);
      throw new IOException("serialization error");
    } catch (IOException e) {
      ctx.close(true);
      throw e;
    }
    try {
      ctx.write(ByteBuffer.wrap(byteOut.toByteArray()));
    } catch (IOException e) {
      // fail this connection
      ctx.close(true);
      throw e;
    }
  }
}
