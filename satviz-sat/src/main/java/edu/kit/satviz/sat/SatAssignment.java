package edu.kit.satviz.sat;

/**
 * This class represents a finished solution for a SAT-instance.
 *
 * @author quorty, LuWae
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
     * This contains the byte representation of the variable state.
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
     * This method converts a given value into an instance of the <code>VariableState</code> enum.
     *
     * @param val Integer representation of the variable state.
     * @return An instance of the <code>VariableState</code> enum,
     *         or <code>null</code> (in case <code>val</code> is invalid).
     */
    public static VariableState fromValue(int val) {
      return switch (val) {
        case 0 -> DONTCARE;
        case 1 -> SET;
        case 2 -> UNSET;
        case 3 -> RESERVED;
        default -> null;
      };
    }
  }

  private final int varCount;
  private final byte[] satAssignment;

  /**
   * An instance of the SatAssignment class can be used similarly to a <code>VariableState</code> array.<br>
   *
   * <p>
   *   <i>NOTE: The variable index starts with <b>1</b> instead of the usual 0.</i>
   *   <code>varCount</code> <i>has to be greater than 0.</i>
   * </p>
   *
   * @param varCount The total amount of different variables.
   */
  public SatAssignment(int varCount) {
    if (varCount > 0) {
      this.varCount = varCount;
      this.satAssignment = new byte[(varCount + 4) >> 2];
    } else {
      this.varCount = 0;
      this.satAssignment = new byte[0];
    }
  }

  /**
   * This method sets the state of a variable.
   *
   * @param variable The variable, whose state is being set.
   * @param state The state as an instance of the <code>VariableState</code> class.
   */
  public void set(int variable, VariableState state) {
    if (variable <= 0 || variable > varCount) {
      return;
    }

    int shift = (variable & 3) << 1;
    byte mask = (byte) ~(3 << shift);

    byte result = (byte) (
            (state.val << shift) | (this.satAssignment[(variable >> 2)] & mask)
    );
    this.satAssignment[(variable >> 2)] = result;
  }

  /**
   * This method returns the state of a variable.
   *
   * @param variable The variable, whose state is being returned.
   * @return The state as an instance of the <code>VariableState</code> class.
   */
  public VariableState get(int variable) {
    if (variable <= 0 || variable > varCount) {
      return VariableState.DONTCARE;
    }

    int shift = (variable & 3) << 1;

    return VariableState.fromValue((this.satAssignment[variable >> 2] >> shift) & 3);
  }

  /**
   * This method returns the integer representation of a variable.
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
   * @param variable The variable, whose state is being returned.
   * @param state The state as an instance of the <code>VariableState</code> class.
   * @return The state as an integer value, that can also hold the variable-ID.
   */
  public static int convertVariableStateToIntState(int variable, VariableState state) {
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
