package edu.kit.satviz.consumer.config.routines;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.Objects;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = NullRoutine.class, name = "NULL"),
    @JsonSubTypes.Type(value = PeriodicRoutine.class, name = "PERIODIC")
})
public abstract class Routine {

  private RoutineType type;

  @JsonIgnore
  public void setType(RoutineType type) {
    this.type = type;
  }

  @JsonIgnore
  public RoutineType getType() {
    return type;
  }

  public abstract void clausesAdded(int clauseAmount);

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Routine routine = (Routine) o;
    return type == routine.type;
  }

  @Override
  public int hashCode() {
    return Objects.hash(type);
  }

  public abstract void addAction(Runnable action);

}
