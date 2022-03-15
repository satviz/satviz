package edu.kit.satviz.network.pub;

import java.net.InetSocketAddress;

/**
 * An identifier for a producer connection, with associated data.
 */
public abstract class ProducerId {
  private final int id;
  private final InetSocketAddress address;
  private final OfferType type;

  protected ProducerId(int id, InetSocketAddress address, OfferType type) {
    this.id = id;
    this.address = address;
    this.type = type;
  }

  protected ProducerId(OfferType type) {
    this.id = -1;
    this.address = null;
    this.type = type;
  }

  /**
   * Returns the ID number the producer described by this ID.
   * @return ID number
   */
  public final int getId() {
    return id;
  }

  /**
   * Returns the remote address of the producer described by this ID.
   * @return remote address
   */
  public final InetSocketAddress getAddress() {
    return address;
  }

  /**
   * Returns the type of the producer described by this ID.
   * @return producer type
   */
  public final OfferType getType() {
    return type;
  }
}
