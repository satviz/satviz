package edu.kit.satviz.network;

import edu.kit.satviz.serial.SerialBuilder;
import edu.kit.satviz.serial.SerializationException;
import edu.kit.satviz.serial.Serializer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.function.Consumer;

public class ClientConnectionManager {
  private final SocketChannel channel;
  private final String address;
  private final int port;
  private final NetworkBlueprint bp;

  private Consumer<ConnectionId> lsConn;
  private Consumer<String> lsFail;
  private Consumer<NetworkMessage> ls;

  private boolean failed = false;
  private boolean finished = false;

  public ClientConnectionManager(String address, int port, NetworkBlueprint bp) throws IOException {
    this.channel = SocketChannel.open();
    this.address = address;
    this.port = port;
    this.bp = bp;
  }

  private void doStart() {
    try {
      channel.connect(new InetSocketAddress(address, port)); // still in blocking mode
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void start() {
    new Thread(
        this::doStart
    ).start();
  }

  private void stop() {

  }

  public void registerConnect(Consumer<ConnectionId> ls) {
    this.lsConn = ls;
  }

  public void registerGlobalFail(Consumer<String> ls) {
    this.lsFail = ls;
  }

  public void register(Consumer<NetworkMessage> ls) {
    this.ls = ls;
  }

  public void send(Object o, byte type) throws IOException {
    Serializer<?> s = bp.getSerializer(type);
    if (s != null) {
      try {
        s.serializeUnsafe(o, channel.socket().getOutputStream());
      } catch (SerializationException e) {
        throw new IllegalArgumentException(e.getMessage());
      } catch (ClassCastException e) {
        throw new IllegalArgumentException("unknown type");
      }
    }
    throw new IllegalArgumentException("unknown type");
  }
}
