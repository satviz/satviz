package edu.kit.satviz.network;

public interface ProducerConnectionListener {

  void onConnect();

  void onDisconnect(String reason);
}
