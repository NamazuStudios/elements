package com.namazustudios.socialengine.service.gameon.client.invoker.builder;

import com.namazustudios.socialengine.model.gameon.game.GameOnSession;
import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.service.gameon.client.exception.PlayerSessionExpiredException;
import com.namazustudios.socialengine.service.gameon.client.invoker.PlayerRequestBuilder;
import io.grpc.Internal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.lang.reflect.Proxy.newProxyInstance;

public abstract class AbstractPlayerRequestBuilder<BuiltT> implements PlayerRequestBuilder<BuiltT> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractPlayerRequestBuilder.class);

    private Client client;

    private GameOnSession gameOnSession;

    private Function<PlayerSessionExpiredException, GameOnSession> unauthorizedHandler = ex -> {
        throw ex;
    };

    private final Class<BuiltT> builtTClass;

    public AbstractPlayerRequestBuilder(Class<BuiltT> builtTClass) {
        this.builtTClass = builtTClass;
    }

    @Override
    public PlayerRequestBuilder<BuiltT> withSession(final GameOnSession gameOnSession) {
        this.gameOnSession = gameOnSession;
        return this;
    }

    @Override
    public PlayerRequestBuilder<BuiltT> withExpirationRetry(final Function<PlayerSessionExpiredException, GameOnSession> unauthorizedHandler) {
//        this.unauthorizedHandler = unauthorizedHandler;
        return this;
    }

    @Override
    public BuiltT build() {
        if (gameOnSession == null) throw new IllegalStateException("session not specified.");
        if (gameOnSession.getSessionId() == null) throw new IllegalStateException("session id not specified");
        if (gameOnSession.getSessionApiKey() == null) throw new IllegalStateException("session api key not specified");

        final InvocationHandler invocationHandler = new InvocationHandler() {

            BuiltT delegate = doBuild(getClient(), gameOnSession);

            @Override
            public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                try {
                    return method.invoke(delegate, args);
                } catch (final InvocationTargetException ex) {

                    final Throwable target = ex.getTargetException();

                    if (target instanceof PlayerSessionExpiredException) {
                        return handle((PlayerSessionExpiredException) target, method, args);
                    } else {
                        throw target;
                    }

                }
            }

            private Object handle(final PlayerSessionExpiredException ex,
                                  final Method method,
                                  final Object[] args) throws Throwable {

                final String msg = ex.getErrorResponse().getMessage();
                logger.info("Player session expired: \"{}\"  Handling.", msg, ex);

                try {
                    final GameOnSession refreshed = unauthorizedHandler.apply(ex);
                    delegate = doBuild(getClient(), refreshed);
                } catch (Exception _ex) {
                    logger.info("Failed to refresh expired session \"{}\"", msg, _ex);
                    throw new InternalException(msg, _ex);
                }

                return retry(method, args);

            }

            private Object retry(final Method method, final Object[] args) throws Throwable {
                try {
                    return method.invoke(delegate, args);
                } catch (PlayerSessionExpiredException ex) {
                    final String msg = ex.getErrorResponse().getMessage();
                    logger.info("Failed to refresh expired session \"{}\"", msg, ex);
                    throw new InternalException(msg, ex);
                }
            }

        };

        return (BuiltT) newProxyInstance(AbstractAdminRequestBuilder.class.getClassLoader(), new Class[]{builtTClass}, invocationHandler);

    }

    /**
     * Performs the actual build operation.
     *
     * @param client
     * @param gameOnSession the {@link GameOnSession}
     * @return the type to build
     */
    protected abstract BuiltT doBuild(Client client, GameOnSession gameOnSession);

    public Client getClient() {
        return client;
    }

    @Inject
    public void setClient(Client client) {
        this.client = client;
    }

}
