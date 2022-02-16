package edu.kit.satviz.network;

import edu.kit.satviz.sat.Clause;
import edu.kit.satviz.sat.ClauseUpdate;
import edu.kit.satviz.sat.SatAssignment;

public class ProducerDemo implements ProducerConnectionListener {
  private static final int PORT = 34312;

  private final Object syncConnect = new Object();
  private volatile boolean connected = false;

  public static void main(String[] args) throws InterruptedException {
    ProducerDemo demo = new ProducerDemo();
    demo.main();
  }

  public void main() throws InterruptedException {
    ProducerConnection conn = new ProducerConnection("localhost", PORT);

    conn.register(this);

    ProducerId pid = new ProducerId(null, OfferType.PROOF, null, false, 0);
    System.out.println("Producer: establishing connection to consumer");
    conn.establish(pid);

    synchronized (syncConnect) {
      while (!connected) {
        syncConnect.wait();
      }
    }

    // TODO load clauses from proof
    conn.sendClauseUpdate(new ClauseUpdate(
        new Clause(
            new int[]{1, 2, 3, 4, 5, 42}), ClauseUpdate.Type.ADD)
    );

    conn.terminateSolved(new SatAssignment(1)); // dummy
    conn.stop();
  }

  @Override
  public void onConnect() {
    System.out.println("Producer: got onConnect");
    synchronized (syncConnect) {
      connected = true;
      syncConnect.notifyAll();
    }
  }

  @Override
  public void onDisconnect(String reason) {
    System.out.println("Producer: got onDisconnect");
  }
}
