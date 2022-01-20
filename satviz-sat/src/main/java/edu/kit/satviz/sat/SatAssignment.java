package edu.kit.satviz.sat;

/**
 * This class represents a finished solution for a SAT-instance.
 *
 * @author quorty, luwae
 */
public class SatAssignment {

  /**
   * This enum represents the variable state.
   */
  public enum VariableState {
    DONTCARE(0),
    SET(1),
    UNSET(2),
    RESERVED(3);

    /**
     * This contains the 2-bit representation of the variable state (stored as a byte).
     */
    public final byte val;

    /**
     * Simple constructor for mapping byte values to the states.
     *
     * @param val Byte representation of the variable state.
     */
    VariableState(int val) {
      this.val = (byte) val;
    }

    /**
     * This method converts a given 2-bit state representation into an instance of the
     * <code>VariableState</code> enum.
     *
     * @param val 2-bit representation of the variable state.
     * @return An instance of the <code>VariableState</code> enum,
     *         or <code>null</code> (in case <code>val</code> is invalid).
     */
    public static VariableState fromValue(int val) {
      for (VariableState state : VariableState.values()) {
        if (state.val == val) {
          return state;
        }
      }
      return null;
    }

    /**
     * This method converts a given integer state representation into an instance of the
     * <code>VariableState</code> enum.
     *
     * @param intState Integer representation of the variable state.
     * @return An instance of the <code>VariableState</code> enum.
     */
    public static VariableState fromIntState(int intState) {
      return switch ((int) Math.signum(intState)) {
        case 0 -> DONTCARE;
        case -1 -> UNSET;
        case 1 -> SET;
        default -> null;
      };
    }

  }

  private final int varCount;
  private final VariableState[] satAssignment;

  /**
   * An instance of the SatAssignment class can be used similarly to a
   * <code>VariableState</code> array.<br>
   *
   * <p>
   * <i>NOTE: The variable index starts with</i> <code>1</code> <i>instead of the usual</i>
   * <code>0</code>. <code>varCount</code> <i>has to be greater than</i> <code>0</code>.
   * </p>
   *
   * @param varCount The total amount of different variables.
   */
  public SatAssignment(int varCount) {
    if (varCount > 0) {
      this.varCount = varCount;
      this.satAssignment = new VariableState[varCount + 1];
    } else {
      this.varCount = 0;
      this.satAssignment = new VariableState[0];
    }
  }

  /**
   * This method sets the state of a variable.<br>
   *
   * <p>
   * <i>NOTE: In case an invalid variable or state is entered, nothing changes.</i>
   * </p>
   *
   * @param variable The variable, whose state is being set.
   * @param state    The state as an instance of the <code>VariableState</code> class.
   */
  public void set(int variable, VariableState state) {
    if (variable <= 0 || variable > this.varCount || state == null) {
      return;
    }
    this.satAssignment[variable] = state;
  }

  /**
   * This method gets the state of a variable as a <code>VariableState</code>.<br>
   *
   * <p>
   * <i>NOTE: In case an invalid variable is entered,</i>
   * <code>DONTCARE</code> <i>is returned.</i>
   * </p>
   *
   * @param variable The variable, whose state is being returned.
   * @return The state as an instance of the <code>VariableState</code> class.
   */
  public VariableState get(int variable) {
    if (variable <= 0 || variable > this.varCount) {
      return VariableState.DONTCARE;
    }
    return this.satAssignment[variable];
  }

  /**
   * This method gets the integer representation of the state of a variable.<br>
   *
   * <p>
   * <i>NOTE: In case an invalid variable is entered,</i>
   * <code>0</code> <i>is returned.</i>
   * </p>
   *
   * @param variable The variable, whose state is being returned.
   * @return The state as an integer value, that can also hold the variable-ID.
   */
  public int getIntState(int variable) {
    return convertVariableStateToIntState(variable, get(variable));
  }

  /**
   * This method converts an instance of the <code>VariableState</code> class
   * into an integer representation of the state.
   *
   * <p>
   * <i>NOTE: In case an invalid state is entered,</i>
   * <code>0</code> <i>is returned.</i>
   * </p>
   *
   * @param variable The variable, whose state is being returned.
   * @param state    The state as an instance of the <code>VariableState</code> class.
   * @return The state as an integer value, that can also hold the variable-ID.
   */
  public static int convertVariableStateToIntState(int variable, VariableState state) {
    if (state == null) {
      return 0;
    }
    return switch (state) {
      case SET -> variable;
      case UNSET -> -variable;
      default -> 0;
    };
  }

  /**
   * This simple getter-method returns the total amount of variables.
   *
   * @return The total amount of variables.
   */
  public int getVarCount() {
    return this.varCount;
  }

}
