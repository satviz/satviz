package edu.kit.satviz.network.pub;

import edu.kit.satviz.network.general.NetworkBlueprint;
import edu.kit.satviz.serial.*;

import java.util.Map;

/**
 * Byte values of the message types of the satviz protocol.
 */
public final class MessageTypes {

  private MessageTypes() {
    // private
  }

  public static final byte OFFER = 1;
  public static final byte START = 2;
  public static final byte STOP = 3;

  public static final byte TERM_SOLVE = 8;
  public static final byte TERM_REFUTE = 9;
  public static final byte TERM_FAIL = 10;

  public static final byte CLAUSE_ADD = 'a';
  public static final byte CLAUSE_DEL = 'd';

  /**
   * All the message types for satviz communication.
   */
  public static final NetworkBlueprint satvizBlueprint = new NetworkBlueprint(
      Map.ofEntries(
          Map.entry(OFFER, new StringMapSerializer()),
          Map.entry(START, new NullSerializer()),
          Map.entry(STOP, new NullSerializer()),
          Map.entry(TERM_SOLVE, new SatAssignmentSerializer()),
          Map.entry(TERM_REFUTE, new NullSerializer()),
          Map.entry(TERM_FAIL, new StringSerializer()),
          Map.entry(CLAUSE_ADD, new ClauseSerializer()),
          Map.entry(CLAUSE_DEL, new ClauseSerializer())
      )
  );
}
