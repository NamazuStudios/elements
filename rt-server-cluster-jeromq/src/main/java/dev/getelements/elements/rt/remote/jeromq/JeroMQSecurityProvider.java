package dev.getelements.elements.rt.remote.jeromq;

import dev.getelements.elements.rt.exception.InvalidPemException;
import dev.getelements.elements.rt.util.PemChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;

public class JeroMQSecurityProvider implements Provider<JeroMQSecurity> {

    private static final Logger logger = LoggerFactory.getLogger(JeroMQSecurityProvider.class);

    public static final String JEROMQ_SERVER_SECURITY_CHAIN_PEM_FILE =
            "dev.getelements.elements.rt.remote.jeromq.server.security.chain.pem.file";

    public static final String JEROMQ_CLIENT_SECURITY_CHAIN_PEM_FILE =
            "dev.getelements.elements.rt.remote.jeromq.client.security.chain.pem.file";

    private Provider<String> serverSecurityChainPemFileProvider;

    private Provider<String> clientSecurityChainPemFileProvider;

    @Override
    public JeroMQSecurity get() {

        final var server = tryLoadChain(getServerSecurityChainPemFileProvider().get());
        final var client = tryLoadChain(getClientSecurityChainPemFileProvider().get());

        if (server.isPresent()) {
            logger.info("Using CURVE JeroMQ Security with server chain.");
            return new JeroMQCurveSecurity(server.get());
        } else if (server.isPresent() && client.isPresent()) {
            logger.info("Using CURVE JeroMQ Security with both client and server chains.");
            return new JeroMQCurveSecurity(server.get(), client.get());
        } else {
            logger.info("Using default JeroMQ Security.");
            return JeroMQSecurity.DEFAULT;
        }

    }

    private static Optional<PemChain> tryLoadChain(final String file) {
        try (var is = new FileInputStream(file)) {
            return Optional.of(new PemChain(is));
        } catch (IOException | InvalidPemException ex) {
            logger.error("Unable to load {}.", file, ex);
            return Optional.empty();
        }
    }

    public Provider<String> getServerSecurityChainPemFileProvider() {
        return serverSecurityChainPemFileProvider;
    }

    @Inject
    public void setServerSecurityChainPemFileProvider(@Named(JEROMQ_SERVER_SECURITY_CHAIN_PEM_FILE) Provider<String> serverSecurityChainPemFileProvider) {
        this.serverSecurityChainPemFileProvider = serverSecurityChainPemFileProvider;
    }

    public Provider<String> getClientSecurityChainPemFileProvider() {
        return clientSecurityChainPemFileProvider;
    }

    @Inject
    public void setClientSecurityChainPemFileProvider(@Named(JEROMQ_CLIENT_SECURITY_CHAIN_PEM_FILE) Provider<String> clientSecurityChainPemFileProvider) {
        this.clientSecurityChainPemFileProvider = clientSecurityChainPemFileProvider;
    }

}
