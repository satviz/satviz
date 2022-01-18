package edu.kit.satviz.network;

import java.net.InetSocketAddress;

/**
 * An ID for an internet connection.
 * This is simply a wrapper for an {@link InetSocketAddress}, but might be extended.
 */
public record ConnectionId(InetSocketAddress addr) {}
