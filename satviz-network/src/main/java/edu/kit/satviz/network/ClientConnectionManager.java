package edu.kit.satviz.network;

import edu.kit.satviz.serial.SerialBuilder;
import edu.kit.satviz.serial.SerializationException;
import edu.kit.satviz.serial.Serializer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ClientConnectionManager {
  private final String address;
  private final int port;
  private final NetworkBlueprint bp;

  private final ConnectionContext context;

  private Consumer<ConnectionId> lsConn;
  private Consumer<String> lsFail;

  private boolean started = false;
  private boolean failed = false;
  private boolean finished = false;

  public ClientConnectionManager(String address, int port, NetworkBlueprint bp) {
    this.context = new ConnectionContext();
    this.address = address;
    this.port = port;
    this.bp = bp;
  }

  private void fail(String reason) {
    failed = true;
    lsFail.accept(reason);
    context.callListener(NetworkMessage.createFail());
  }

  private void doStart() {
    try {
      context.setChannel(SocketChannel.open());
    } catch (IOException e) {
      failed = true;
      lsFail.accept("socket channel could not be created");
    }
    try {
      channel.connect(new InetSocketAddress(address, port)); // still in blocking mode
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public boolean start() {
    if (started || failed || finished) {
      return false;
    }
    started = true;
    new Thread(
        this::doStart
    ).start();
    return true;
  }

  private boolean stop() {
    if (!started || failed || finished) {
      return false;
    }
    finished = true;
    return true;
  }

  public void registerConnect(Consumer<ConnectionId> ls) {
    this.lsConn = ls;
  }

  public void registerGlobalFail(Consumer<String> ls) {
    this.lsFail = ls;
  }

  public void register(BiConsumer<ConnectionId, NetworkMessage> ls) {
    context.setListener(ls);
  }

  public void send(byte type, Object obj) throws IOException {
    context.send(type, obj);
  }
}
