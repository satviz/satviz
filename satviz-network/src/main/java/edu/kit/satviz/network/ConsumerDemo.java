package edu.kit.satviz.network;

import edu.kit.satviz.sat.ClauseUpdate;
import edu.kit.satviz.sat.SatAssignment;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConsumerDemo implements ConsumerConnectionListener {
  private static final int PORT = 34312;

  private final List<ProducerId> pids = new ArrayList<>();
  private volatile boolean finished = false;
  private final Object syncTerm = new Object();

  public static void main(String[] args) throws InterruptedException {
    ConsumerDemo demo = new ConsumerDemo();
    demo.main();
  }

  public void main() throws InterruptedException {
    ConsumerConnection conn = new ConsumerConnection(PORT);

    conn.registerConnect(this::connectListener);
    conn.start();

    synchronized (pids) {
      while (pids.isEmpty()) {
        pids.wait();
      }
    }
    ProducerId pid = pids.get(0);
    System.out.println("Consumer: got Producer: " + pid.cid().address());
    if (pid.type() == OfferType.SOLVER) {
      System.out.println("type=SOLVER, name=" + pid.solverName() + ", solverDelayed=" + pid.solverDelayed());
    } else {
      System.out.println("type=PROOF");
    }

    System.out.println("Consumer: starting connection");
    conn.connect(pid, this);

    synchronized (syncTerm) {
      while (!finished) {
        syncTerm.wait();
      }
    }

    conn.stop();
  }

  private void connectListener(ProducerId pid) {
    synchronized (pids) {
      pids.add(pid);
      pids.notifyAll();
    }
  }

  @Override
  public void onClauseUpdate(ProducerId pid, ClauseUpdate c) {
    System.out.println("Consumer: got clause update: " + c.type() + " "
        + Arrays.toString(c.clause().literals()));
  }

  @Override
  public void onTerminateSolved(ProducerId pid, SatAssignment assign) {
    System.out.println("Consumer: got terminate solved");
    synchronized (syncTerm) {
      finished = true;
      syncTerm.notifyAll();
    }
  }

  @Override
  public void onTerminateRefuted(ProducerId pid) {
    System.out.println("Consumer: got terminate refuted");
    synchronized (syncTerm) {
      finished = true;
      syncTerm.notifyAll();
    }
  }

  @Override
  public void onTerminateOtherwise(ProducerId pid, String reason) {
    System.out.println("Consumer: got terminate otherwise: " + reason);
    synchronized (syncTerm) {
      finished = true;
      syncTerm.notifyAll();
    }
  }
}
