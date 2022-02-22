package edu.kit.satviz.network;

import edu.kit.satviz.sat.Clause;
import edu.kit.satviz.sat.ClauseUpdate;
import edu.kit.satviz.sat.SatAssignment;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ProducerConsumerTest implements ProducerConnectionListener, ConsumerConnectionListener {

  private volatile String prodFail = null;
  private volatile String consFail = null;

  private final Object syncConnect1 = new Object();
  private final Object syncConnect2 = new Object();
  private final Object syncTerm = new Object();
  private final Object syncClauseUpdate = new Object();

  private boolean prodOnConnect = false;
  private String prodOnDisconnect = null;
  private List<ProducerId> consConnections = new ArrayList<>();
  private Map<ProducerId, List<ClauseUpdate>> clauseUpdates = new HashMap<>();
  private Map<ProducerId, SatAssignment> termSolved = new HashMap<>();
  private Set<ProducerId> termRefuted = new HashSet<>();
  private Map<ProducerId, String> termOtherwise = new HashMap<>();

  private static final int PORT = 35124;

  @Test
  void connectionTest() throws InterruptedException {
    ProducerConnection prod = new ProducerConnection("localhost", PORT);
    ConsumerConnection cons = new ConsumerConnection(PORT);

    try {
      prod.registerGlobalFail(this::prodFailListener);
      prod.register(this);
      cons.registerGlobalFail(this::consFailListener);
      cons.registerConnect(this::consConnectListener);

      ProducerId establishPid = new ProducerId(null, OfferType.SOLVER, "cadical", true, 42);
      prod.establish(establishPid);

      cons.start();

      synchronized (syncConnect1) {
        while (consConnections.isEmpty()) {
          syncConnect1.wait();
        }
      }

      ProducerId consId = consConnections.get(0);
      assertEquals(OfferType.SOLVER, consId.type());
      assertEquals("cadical", consId.solverName());
      assertTrue(consId.solverDelayed());
      assertEquals(42, consId.instanceHash());

      cons.connect(consId, this);

      synchronized (syncConnect2) {
        while (!prodOnConnect) {
          syncConnect2.wait();
        }
      }

      ClauseUpdate c1 = new ClauseUpdate(new Clause(new int[]{1, 2, 3}), ClauseUpdate.Type.ADD);
      ClauseUpdate c2 = new ClauseUpdate(new Clause(new int[]{42, 43, 44}), ClauseUpdate.Type.REMOVE);
      try {
        prod.sendClauseUpdate(c1);
        prod.sendClauseUpdate(c2);
      } catch (IllegalStateException e) {
        fail("illegal state on sending clause update");
      }

      synchronized (syncClauseUpdate) {
        while (clauseUpdates.get(consId) == null || clauseUpdates.get(consId).size() != 2) {
          syncClauseUpdate.wait();
        }
      }

      ClauseUpdate recC1 = clauseUpdates.get(consId).get(0);
      ClauseUpdate recC2 = clauseUpdates.get(consId).get(1);
      assertEquals(c1.clause(), recC1.clause());
      assertEquals(c1.type(), recC1.type());
      assertEquals(c2.clause(), recC2.clause());
      assertEquals(c2.type(), recC2.type());

      SatAssignment sol = new SatAssignment(10);
      sol.set(5, SatAssignment.VariableState.SET);
      sol.set(7, SatAssignment.VariableState.UNSET);
      prod.terminateSolved(sol);

      synchronized (syncTerm) {
        while (termSolved.isEmpty() || termSolved.get(consId) == null) {
          syncTerm.wait();
        }
      }

      assertEquals(sol, termSolved.get(consId));

    } finally {
      try {
        prod.stop();
        cons.stop();
      } catch (InterruptedException e) {
        fail(e);
      }
    }
    assertNull(prodFail);
    assertNull(consFail);
  }

  private void consFailListener(String reason) {
    consFail = reason;
  }

  private void prodFailListener(String reason) {
    prodFail = reason;
  }

  public void onConnect() {
    synchronized (syncConnect2) {
      prodOnConnect = true;
      syncConnect2.notifyAll();
    }
  }

  public void onDisconnect(String reason) {
    prodOnDisconnect = reason;
  }

  private void consConnectListener(ProducerId pid) {
    synchronized (syncConnect1) {
      consConnections.add(pid);
      syncConnect1.notifyAll();
    }
  }

  public void onClauseUpdate(ProducerId pid, ClauseUpdate c) {
    synchronized (syncClauseUpdate) {
      List<ClauseUpdate> updates = clauseUpdates.computeIfAbsent(pid, k -> new ArrayList<>());
      updates.add(c);
      syncClauseUpdate.notifyAll();
    }
  }

  public void onTerminateSolved(ProducerId pid, SatAssignment assign) {
    synchronized (syncTerm) {
      termSolved.put(pid, assign);
      syncTerm.notifyAll();
    }
  }

  public void onTerminateRefuted(ProducerId pid) {
    synchronized (syncTerm) {
      termRefuted.add(pid);
      syncTerm.notifyAll();
    }
  }

  public void onTerminateOtherwise(ProducerId pid, String reason) {
    synchronized (syncTerm) {
      termOtherwise.put(pid, reason);
      syncTerm.notifyAll();
    }
  }
}
