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

  public final int id() {
    return id;
  }

  public final InetSocketAddress address() {
    return address;
  }

  public final OfferType type() {
    return type;
  }
}
