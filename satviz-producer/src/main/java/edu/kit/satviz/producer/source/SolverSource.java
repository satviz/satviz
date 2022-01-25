package edu.kit.satviz.producer.source;

import edu.kit.ipasir4j.Solver;
import edu.kit.satviz.producer.ClauseSource;
import edu.kit.satviz.producer.SourceException;

public class SolverSource extends ClauseSource {

  private final Solver solver;
  private final int varCount;

  public SolverSource(Solver solver, int varCount) {
    this.solver = solver;
    this.varCount = varCount;
  }


  @Override
  public void open() throws SourceException {

  }

  @Override
  public void close() throws Exception {

  }
}
