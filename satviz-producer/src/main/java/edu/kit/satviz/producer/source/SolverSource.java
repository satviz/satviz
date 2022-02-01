package edu.kit.satviz.producer.source;

import edu.kit.ipasir4j.AbstractLearnCallback;
import edu.kit.ipasir4j.AbstractTerminateCallback;
import edu.kit.ipasir4j.NullData;
import edu.kit.ipasir4j.Solver;
import edu.kit.satviz.producer.ClauseSource;
import edu.kit.satviz.sat.Clause;
import edu.kit.satviz.sat.ClauseUpdate;
import edu.kit.satviz.sat.SatAssignment;
import java.util.function.Consumer;
import jdk.incubator.foreign.MemoryAddress;

/**
 * An implementation of {@link ClauseSource} representing a CDCL solver emitting the clauses it
 * learns while solving a SAT instance.
 */
public class SolverSource extends ClauseSource {

  private final Solver solver;
  private final int varCount;

  private volatile boolean shouldTerminate;

  /**
   * Creates a source from an ipasir4j {@code Solver} and the amount of variables the corresponding
   * instance contains.
   *
   * @param solver a {@code Solver} which is already configured with a SAT instance, i.e. all the
   *               clauses have been added already. If learn or terminate callbacks are set, they
   *               will be overwritten.
   * @param varCount The amount of variables contained in the SAT instance the solver is solving.
   */
  public SolverSource(Solver solver, int varCount) {
    this.solver = solver;
    this.varCount = varCount;
    this.shouldTerminate = false;
    solver.setLearn(MemoryAddress.NULL, varCount, new LearnCallback());
    solver.setTerminate(MemoryAddress.NULL, new TerminateCallback());
  }

  /**
   * Opens the source by starting the solver. A clause will be emitted by the solver when it learns
   * one.
   *
   * <p>When done, either calls the function set by {@link #whenSolved(Consumer)} if the solver has
   * found a solution or the function set by {@link #whenRefuted(Runnable)} if the solver has found
   * that there is no solution.
   *
   * <p>This method closes the underlying solver.
   */
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

  /**
   * ipasir terminate callback for solver sources, for internal use.
   */
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

  /**
   * ipasir learn callback for solver sources, for internal use.
   */
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
