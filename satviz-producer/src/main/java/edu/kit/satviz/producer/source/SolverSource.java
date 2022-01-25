package edu.kit.satviz.producer.source;

import edu.kit.ipasir4j.AbstractLearnCallback;
import edu.kit.ipasir4j.AbstractTerminateCallback;
import edu.kit.ipasir4j.NullData;
import edu.kit.ipasir4j.Solver;
import edu.kit.satviz.producer.ClauseSource;
import edu.kit.satviz.sat.Clause;
import edu.kit.satviz.sat.ClauseUpdate;
import edu.kit.satviz.sat.SatAssignment;
import jdk.incubator.foreign.MemoryAddress;

public class SolverSource extends ClauseSource {

  private final Solver solver;
  private final int varCount;

  private volatile boolean shouldTerminate;

  public SolverSource(Solver solver, int varCount) {
    this.solver = solver;
    this.varCount = varCount;
    this.shouldTerminate = false;
    solver.setLearn(MemoryAddress.NULL, varCount, new LearnCallback());
    solver.setTerminate(MemoryAddress.NULL, new TerminateCallback());
  }

  @Override
  public void open() {
    Solver.Result result = solver.solve();
    switch (result) {
      case SATISFIABLE -> solvedListener.accept(solvingAssignment());
      case UNSATISFIABLE -> refutedListener.run();
      default -> {
        /* Do nothing - premature termination of the source can only
        come purposefully from the network connection, so it can be handled with a no-op. */
      }
    }
    solver.close();
  }

  private SatAssignment solvingAssignment() {
    SatAssignment assignment = new SatAssignment(varCount);
    for (int i = 1; i <= varCount; i++) {
      assignment.set(i, SatAssignment.VariableState.fromIntState(solver.val(i)));
    }
    return assignment;
  }

  @Override
  public void close() {
    shouldTerminate = true;
  }

  public class TerminateCallback extends AbstractTerminateCallback<NullData> {

    @Override
    protected boolean onTerminateQuestion(NullData data) {
      return shouldTerminate;
    }

    @Override
    public NullData dataFrom(MemoryAddress dataAddr) {
      return null;
    }
  }

  public class LearnCallback extends AbstractLearnCallback<NullData> {

    @Override
    protected void onClauseLearn(NullData data, int[] clause) {
      clauseListener.accept(new ClauseUpdate(new Clause(clause), ClauseUpdate.Type.ADD));
    }

    @Override
    public NullData dataFrom(MemoryAddress dataAddr) {
      return null;
    }
  }
}
