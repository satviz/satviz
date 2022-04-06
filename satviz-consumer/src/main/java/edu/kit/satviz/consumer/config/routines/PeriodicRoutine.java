package edu.kit.satviz.consumer.config.routines;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.Objects;

@JsonTypeName("PERIODIC")
public class PeriodicRoutine extends Routine {

  public static final int DEFAULT_CLAUSE_PERIOD = 50;

  @JsonProperty("clausePeriod")
  private int clausePeriod;

  private Runnable action;
  private int counter = 0;

  public PeriodicRoutine() {
    this.clausePeriod = DEFAULT_CLAUSE_PERIOD;
    super.setType(RoutineType.PERIODIC);
  }

  public void setClausePeriod(int clausePeriod) {
    this.clausePeriod = clausePeriod;
  }

  public int getClausePeriod() {
    return clausePeriod;
  }

  @Override
  public void clausesAdded(int clauseAmount) {
    if (clauseAmount < 0) {
      throw new IllegalArgumentException("Clause amount should be greater than 0");
    }
    counter += clauseAmount;
    if (counter > clausePeriod) {
      counter %= clausePeriod;
      action.run();
    }
  }

  @Override
  public void addAction(Runnable action) {
    if (action == null) {
      throw new IllegalArgumentException("Action should not be null");
    }
    this.action = action;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PeriodicRoutine that = (PeriodicRoutine) o;
    return clausePeriod == that.clausePeriod && counter == that.counter && Objects.equals(action, that.action);
  }

  @Override
  public int hashCode() {
    return Objects.hash(clausePeriod, action, counter);
  }

}
