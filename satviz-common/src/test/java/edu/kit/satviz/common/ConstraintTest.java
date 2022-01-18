package edu.kit.satviz.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConstraintTest {

  @Test
  void testChecking() {
    Constraint<String> emptyConstraint = Constraint.checking(String::isEmpty, "String is not empty");
    assertThrows(ConstraintValidationException.class, () -> emptyConstraint.validate("hello"));
    assertDoesNotThrow(() -> emptyConstraint.validate(""));
  }

  @Test
  void testOn() {
    Constraint<Integer> posIntConstraint = Constraint.checking(i -> i > 0, "Number is not positive");
    Constraint<String> posStringConstraint = posIntConstraint.on(Integer::parseInt);
    assertThrows(ConstraintValidationException.class, () -> posStringConstraint.validate("-42"));
    assertDoesNotThrow(() -> posStringConstraint.validate("2137423"));
  }

  @Test
  void testAllOf() {
    Constraint<String> succeeding = (s) -> {};
    Constraint<String> failing = (s) -> { throw new ConstraintValidationException("fail"); };
    assertThrows(
        ConstraintValidationException.class,
        () -> Constraint.allOf(succeeding, succeeding, failing).validate("")
    );

    assertThrows(
        ConstraintValidationException.class,
        () -> Constraint.allOf(failing, succeeding, succeeding).validate("")
    );

    assertThrows(
        ConstraintValidationException.class,
        () -> Constraint.allOf(succeeding, failing, succeeding).validate("")
    );

    assertDoesNotThrow(
        () -> Constraint.allOf(succeeding, succeeding, succeeding).validate("")
    );
  }

}
