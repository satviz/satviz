package edu.kit.satviz.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConstraintTest {

  private Constraint<String> succeeding;
  private Constraint<String> failing;

  @BeforeEach
  void setUp() {
    succeeding = (s) -> {
    };
    failing = (s) -> {
      throw new ConstraintValidationException("fail");
    };
  }

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

  @Test
  void testOneOf() {
    assertThrows(
        ConstraintValidationException.class,
        () -> Constraint.oneOf(failing, failing, failing).validate("")
    );

    assertDoesNotThrow(
        () -> Constraint.oneOf(failing, succeeding, succeeding).validate("")
    );

    assertDoesNotThrow(
        () -> Constraint.oneOf(succeeding, failing, succeeding).validate("")
    );

    assertDoesNotThrow(
        () -> Constraint.oneOf(succeeding, succeeding, succeeding).validate("")
    );
  }

}
