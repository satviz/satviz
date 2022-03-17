package edu.kit.satviz.network;

import edu.kit.satviz.network.pub.*;
import edu.kit.satviz.sat.Clause;
import edu.kit.satviz.sat.ClauseUpdate;
import edu.kit.satviz.sat.SatAssignment;
import org.junit.jupiter.api.Test;

import java.lang.invoke.StringConcatException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ProducerConsumerTest implements ProducerConnectionListener, ConsumerConnectionListener {
  private static ProducerConnection prod;
  private static ConsumerConnection cons;
  private static final Object SYNC_PROD = new Object();
  private static final Object SYNC_CONS = new Object();

  private static List<ProducerId> lsConnectCalls = new ArrayList<>();
  private static List<String> lsFailCalls = new ArrayList<>();

  private static Map<ProducerId, List<ClauseUpdate>> onClauseUpdateCalls = new HashMap<>();
  private static Map<ProducerId, SatAssignment> onTerminateSolvedCalls = new HashMap<>();
  private static List<ProducerId> onTerminateRefutedCalls = new ArrayList<>();
  private static Map<ProducerId, String> onTerminateOtherCalls = new HashMap<>();

  private static int onConnectCalls = 0;
  private static List<String> onDisconnectCalls = new ArrayList<>();

  @Test
  void testLocalhost() {
    final int PORT = 34312;
    try {
      prod = new ProducerConnection("localhost", PORT,
          new SolverId("cadical", false, 42),
          this
          );
      prod.establish();

      cons = new ConsumerConnection(PORT, this::lsConnect, this::lsFail);
      cons.start();

      synchronized (SYNC_PROD) {
        while (onConnectCalls == 0) {
          SYNC_PROD.wait();
        }
      }
      synchronized (SYNC_CONS) {
        while(lsConnectCalls.isEmpty()) {
          SYNC_CONS.wait();
        }
      }
      // consumer and producer found each other
      assertEquals(OfferType.SOLVER, lsConnectCalls.get(0).getType());
      SolverId sid = (SolverId) lsConnectCalls.get(0);
      assertEquals("cadical", sid.getSolverName());
      assertFalse(sid.isSolverDelayed());
      assertEquals(42, sid.getInstanceHash());


      ClauseUpdate c1 = new ClauseUpdate(new Clause(new int[]{1,-1,400000}), ClauseUpdate.Type.ADD);
      ClauseUpdate c2 = new ClauseUpdate(new Clause(new int[]{1,2,3,4,5,-1,-2,-3,-4,-5}), ClauseUpdate.Type.REMOVE);
      SatAssignment assign = new SatAssignment(10);
      assign.set(5, SatAssignment.VariableState.SET);
      assertTrue(prod.sendClauseUpdate(c1));
      assertTrue(prod.sendClauseUpdate(c2));
      prod.terminateSolved(assign);
      assertFalse(prod.sendClauseUpdate(c1)); // sending after terminating

      synchronized (SYNC_CONS) {
        while (onTerminateSolvedCalls.isEmpty()) {
          SYNC_CONS.wait();
        }
      }
      List<ClauseUpdate> updates = onClauseUpdateCalls.get(sid);
      assertNotNull(updates);
      assertEquals(2, updates.size());
      assertEquals(c1, updates.get(0));
      assertEquals(c2, updates.get(1));
      assertTrue(onDisconnectCalls.isEmpty()); // prod disconnected, so no message

    } catch (Throwable t) {
      fail(t);
    } finally {
      if (prod != null) prod.terminateOtherwise("finally");
      if (cons != null) cons.stop();
    }
  }

  @Override
  public void onConnect() {
    synchronized (SYNC_PROD) {
      onConnectCalls++;
      SYNC_PROD.notifyAll();
    }
  }

  @Override
  public void onDisconnect(String reason) {
    synchronized (SYNC_PROD) {
      onDisconnectCalls.add(reason);
      SYNC_PROD.notifyAll();
    }
  }

  public void lsConnect(ProducerId pid) {
    synchronized (SYNC_CONS) {
      lsConnectCalls.add(pid);
      cons.connect(pid, this);
      SYNC_CONS.notifyAll();
    }
  }

  public void lsFail(String msg) {
    synchronized (SYNC_CONS) {
      lsFailCalls.add(msg);
      SYNC_CONS.notifyAll();
    }
  }

  @Override
  public void onClauseUpdate(ProducerId pid, ClauseUpdate c) {
    synchronized (SYNC_CONS) {
      onClauseUpdateCalls.computeIfAbsent(pid, (p) -> new ArrayList<>()).add(c);
      SYNC_CONS.notifyAll();
    }
  }

  @Override
  public void onTerminateSolved(ProducerId pid, SatAssignment assign) {
    synchronized (SYNC_CONS) {
      onTerminateSolvedCalls.put(pid, assign);
      SYNC_CONS.notifyAll();
    }
  }

  @Override
  public void onTerminateRefuted(ProducerId pid) {
    synchronized (SYNC_CONS) {
      onTerminateRefutedCalls.add(pid);
      SYNC_CONS.notifyAll();
    }
  }

  @Override
  public void onTerminateOtherwise(ProducerId pid, String reason) {
    synchronized (SYNC_CONS) {
      onTerminateOtherCalls.put(pid, reason);
      SYNC_CONS.notifyAll();
    }
  }
}
