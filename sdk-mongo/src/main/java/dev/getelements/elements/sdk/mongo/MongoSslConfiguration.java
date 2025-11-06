package dev.getelements.elements.sdk.mongo;

import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;

/**
 * The MongoDB SSL configuration record. Contains SSL Specific configuration for MongoDB connections.
 */
public record MongoSslConfiguration(KeyManager[] keyManager, TrustManager[] trustManager) {}
