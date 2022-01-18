package edu.kit.satviz.network;

import java.util.List;
import java.util.function.Consumer;

public class ConsumerConnectionManager {

  public void start() {
    // TODO
  }

  public void stop() {
    // TODO
  }

  public List<ProducerId> getProducers() {
    return null; // TODO
  }

  public List<ProducerId> getFreeProducers() {
    return null; // TODO
  }

  public void registerGlobalFail(Consumer<String> ls) {
    // TODO
  }

  public void connect(ProducerId pid, ConsumerConnectionListener ls) {
    // TODO
  }

  public void disconnect(ProducerId pid, ConsumerConnectionListener ls) {
    // TODO
  }
}
