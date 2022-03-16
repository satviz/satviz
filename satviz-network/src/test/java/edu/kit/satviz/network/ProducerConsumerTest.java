package edu.kit.satviz.network;

import edu.kit.satviz.network.pub.ConsumerConnection;
import edu.kit.satviz.network.pub.ProducerConnection;
import edu.kit.satviz.network.pub.ProducerConnectionListener;
import edu.kit.satviz.network.pub.ProducerId;
import org.junit.jupiter.api.Test;

public class ProducerConsumerTest implements ProducerConnectionListener {
  private static ProducerConnection prod;
  private static ConsumerConnection cons;

  @Test
  void testLocalhost() {
    final int PORT = 34312;
    try {
      cons = new ConsumerConnection(PORT, this::lsConnect, this::lsFail);
    } catch (Throwable t) {

    } finally {
      if (prod != null) prod.terminateOtherwise("finally");
      if (cons != null) cons.stop();
    }
  }

  @Override
  public void onConnect() {
    // TODO
  }

  @Override
  public void onDisconnect(String reason) {
    // TODO
  }

  public void lsConnect(ProducerId pid) {
    // TODO
  }

  public void lsFail(String msg) {
    // TODO
  }
}
