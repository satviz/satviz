package edu.kit.satviz.consumer.config.routines;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("NULL")
public class NullRoutine extends Routine {

  public NullRoutine() {
    super.setType(RoutineType.NULL);
  }

  @Override
  public void clausesAdded(int clauseAmount) {
    // do nothing.
  }

  @Override
  public void addAction(Runnable action) {
    // do nothing.
  }

}
