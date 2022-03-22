package edu.kit.satviz.consumer.processing;

import edu.kit.satviz.sat.Clause;
import edu.kit.satviz.sat.ClauseUpdate;
import java.util.Arrays;

public class Constants {

  static final Clause[] CLAUSES = {
      new Clause(new int[] {1, 6, -5, 3}),
      new Clause(new int[] {5, -4, 2}),
      new Clause(new int[] {1, -2, 3}),
      new Clause(new int[] {7, 4, -1}),
      new Clause(new int[] {-6, 2})
  };

  static final ClauseUpdate[] UPDATES = Arrays.stream(CLAUSES)
      .map(c -> new ClauseUpdate(c, ClauseUpdate.Type.ADD))
      .toArray(ClauseUpdate[]::new);

}
