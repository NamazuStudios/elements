package dev.getelements.elements.rt.remote.jeromq;

import dev.getelements.elements.rt.exception.InternalException;
import dev.getelements.elements.sdk.model.security.InvalidPemException;
import dev.getelements.elements.sdk.model.security.PemChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;

public class JeroMQSecurityProvider implements Provider<JeroMQSecurity> {

    private static final Logger logger = LoggerFactory.getLogger(JeroMQSecurityProvider.class);

    public static final String JEROMQ_ALLOW_PLAIN_TRAFFIC =
            "dev.getelements.elements.rt.remote.jeromq.allow.plain.traffic";

    public static final String JEROMQ_SERVER_SECURITY_CHAIN_PEM_FILE =
            "dev.getelements.elements.rt.remote.jeromq.server.security.chain.pem.file";

    private Provider<Boolean> allowPlainTrafficProvider;

    private Provider<String> serverSecurityChainPemFileProvider;

    @Override
    public JeroMQSecurity get() {

        final var server = tryLoadChain(getServerSecurityChainPemFileProvider().get());

        if (server.isPresent()) {
            logger.info("Using CURVE JeroMQ Security with server chain.");
            return new JeroMQCurveSecurity(server.get());
        } else if (getAllowPlainTrafficProvider().get()) {
            logger.info("Using default JeroMQ Security.");
            return JeroMQSecurity.DEFAULT;
        } else {
            throw new InternalException("Invalid security configuration.");
        }

    }

    private static Optional<PemChain> tryLoadChain(final String file) {

        if (file.isBlank()) {
            logger.info("No pem file specified.");
            return Optional.empty();
        }

        try (var is = new FileInputStream(file)) {
            final var chain = new PemChain(is);
            return Optional.of(chain);
        } catch (IOException | InvalidPemException ex) {
            logger.error("Unable to load {}.", file, ex);
            return Optional.empty();
        }

    }

    public Provider<Boolean> getAllowPlainTrafficProvider() {
        return allowPlainTrafficProvider;
    }

    @Inject
    public void setAllowPlainTrafficProvider(@Named(JEROMQ_ALLOW_PLAIN_TRAFFIC) Provider<Boolean> allowPlainTrafficProvider) {
        this.allowPlainTrafficProvider = allowPlainTrafficProvider;
    }

    public Provider<String> getServerSecurityChainPemFileProvider() {
        return serverSecurityChainPemFileProvider;
    }

    @Inject
    public void setServerSecurityChainPemFileProvider(@Named(JEROMQ_SERVER_SECURITY_CHAIN_PEM_FILE) Provider<String> serverSecurityChainPemFileProvider) {
        this.serverSecurityChainPemFileProvider = serverSecurityChainPemFileProvider;
    }

}
