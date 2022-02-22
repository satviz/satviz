package edu.kit.satviz.producer.mode;

import edu.kit.ipasir4j.Ipasir;
import edu.kit.ipasir4j.IpasirNotFoundException;
import edu.kit.ipasir4j.Solver;
import edu.kit.satviz.common.Hashing;
import edu.kit.satviz.network.OfferType;
import edu.kit.satviz.network.ProducerId;
import edu.kit.satviz.parsers.DimacsFile;
import edu.kit.satviz.parsers.ParsingException;
import edu.kit.satviz.producer.ProducerMode;
import edu.kit.satviz.producer.ProducerModeData;
import edu.kit.satviz.producer.SourceException;
import edu.kit.satviz.producer.cli.ProducerParameters;
import edu.kit.satviz.producer.source.SolverSource;
import edu.kit.satviz.sat.ClauseUpdate;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A mode for when the producer should get its clauses from a CDCL/ipasir-compliant solver.
 */
public class SolverMode implements ProducerMode {

  @Override
  public boolean isSet(ProducerParameters parameters) {
    return parameters.getSolverFile() != null && parameters.getInstanceFile() != null;
  }

  @Override
  public ProducerModeData apply(ProducerParameters parameters) throws SourceException {
    tryLoadSolver(parameters.getSolverFile());
    try (DimacsFile instance = new DimacsFile(Files.newInputStream(parameters.getInstanceFile()))) {
      Solver solver = Ipasir.init();
      configureSolver(solver, instance);
      return new ProducerModeData(
          new SolverSource(solver, instance.getVariableAmount()),
          new ProducerId(null, OfferType.SOLVER, Ipasir.signature(),
              parameters.isNoWait(),
              Hashing.hashContent(Files.newInputStream(parameters.getInstanceFile())))
      );
    } catch (IOException e) {
      throw new SourceException("I/O exception trying to read instance file", e);
    } catch (ParsingException e) {
      throw new SourceException("Error while parsing DIMACS CNF file", e);
    } catch (IpasirNotFoundException e) {
      throw new SourceException("Ipasir function(s) not found in shared library", e);
    }

  }

  private void tryLoadSolver(Path path) throws SourceException {
    try {
      System.load(path.toAbsolutePath().toString());
    } catch (UnsatisfiedLinkError e) {
      throw new SourceException("Could not load solver shared library", e);
    }
  }

  private void configureSolver(Solver solver, DimacsFile instance) {
    for (ClauseUpdate update : instance) {
      int[] literals = update.clause().literals();
      for (int literal : literals) {
        solver.add(literal);
      }
      solver.add(0);
    }
  }
}
