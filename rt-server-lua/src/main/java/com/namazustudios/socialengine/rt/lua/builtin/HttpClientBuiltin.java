package com.namazustudios.socialengine.rt.lua.builtin;

import com.namazustudios.socialengine.jnlua.JavaFunction;
import com.namazustudios.socialengine.jnlua.LuaState;
import com.namazustudios.socialengine.rt.Context;
import com.namazustudios.socialengine.rt.TaskId;
import com.namazustudios.socialengine.rt.lua.persist.Persistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.client.*;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.concurrent.CompletionStage;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.namazustudios.socialengine.rt.lua.builtin.BuiltinUtils.currentTaskId;
import static com.namazustudios.socialengine.rt.lua.builtin.coroutine.YieldInstruction.INDEFINITELY;
import static java.lang.String.format;
import static javax.ws.rs.HttpMethod.POST;
import static javax.ws.rs.HttpMethod.PUT;
import static javax.ws.rs.core.Response.Status.Family.SUCCESSFUL;

public class HttpClientBuiltin implements Builtin {

    private static final Logger logger = LoggerFactory.getLogger(HttpClientBuiltin.class);

    public static final String MODULE_NAME = "namazu.http.client";

    public static final String SEND = "send";

    private Client client;

    private Context context;

    private final JavaFunction send = l -> {

        final TaskId taskId = currentTaskId(l);
        if (taskId == null) throw new IllegalStateException("No currently running task.");

        final String base = getRequiredStringField(l, "base");

        WebTarget target = getClient().target(base);
        target = getRequiredStringField(l, "path", target::path);
        target = getOptionalMultiField(l, "params", target, target::queryParam);
        target = getOptionalMultiField(l, "matrix", target, target::matrixParam);

        final Invocation.Builder builder = target.request();
        getOptionalMultiField(l, "headers", builder::header);
        getOptionalMultiField(l, "cookies", builder::cookie);
        getOptionalStringFields(l, "accept", builder::accept);
        getOptionalStringFields(l, "accept_language", builder::acceptLanguage);

        final String method = getRequiredStringField(l, "method");
        final Entity<Object> requestEntity = getEntity(l);

        final CompletionStage<Response> responseCompletionStage =
            (PUT.equals(method) || POST.equals(method)) && (requestEntity != null) ?
                builder.rx().method(method, requestEntity, new GenericType<Response>(){}) :
                builder.rx().method(method, new GenericType<Response>(){});

        if (logger.isDebugEnabled()) {
            final StringBuilder req = new StringBuilder();
            req.append(format("%s %s\n", method, target.getUri()));
            getOptionalMultiField(l, "headers", (k, v) -> req.append(k).append(": ").append(v).append('\n'));
            logger.debug("HTTP Request: \n{}\n", req);
        }

        responseCompletionStage
            .exceptionally(th -> null)
            .handleAsync((response, throwable) -> {

                logger.debug("Got response {}", response, throwable);

                try {
                    if (response == null) {
                        getContext().getSchedulerContext().resumeWithError(taskId, throwable);
                    } else {

                        final int status = response.getStatus();
                        final MultivaluedMap headers = response.getHeaders();
                        final Object responseEntity = SUCCESSFUL.equals(response.getStatusInfo().getFamily()) ?
                            response.readEntity(Object.class) :
                            null;

                        logger.trace("Status: {}.  Headers: {}.  Entity: {}", status, headers, responseEntity);
                        getContext().getSchedulerContext().resume(taskId, status, headers, responseEntity);

                    }
                }catch (Exception ex) {
                    getContext().getSchedulerContext().resumeWithError(taskId, throwable);
                }

                return null;

            });

        l.pushJavaObject(INDEFINITELY.toString());
        return l.yield(1);

    };

    private String getRequiredStringField(final LuaState l, final String key) {
        try {
            l.getField(1, key);
            if (l.isNil( -1)) throw new IllegalArgumentException(key + " must be specified ");
            return l.toString(-1);
        } finally {
            l.pop(1);
        }
    }

    private <T> T getRequiredStringField(final LuaState l, final String key, final Function<String, T> consumer) {
        try {
            l.getField(1, key);
            if (l.isNil( -1)) throw new IllegalArgumentException(key + " must be specified ");
            return consumer.apply(l.toString(-1));
        } finally {
            l.pop(1);
        }
    }

    private void getOptionalStringFields(final LuaState l, final String key, final Consumer<String> consumer) {
        try {

            l.getField(1, key);

            if (l.isString(-1) || l.isNumber(-1)) {
                consumer.accept(l.toString(-1));
            } else if (l.isTable(-1)) {
                Stream.of(l.toJavaObject(-1, String[].class)).forEach(v -> consumer.accept(v));
            } else if (!l.isNil(-1)) {
                throw new IllegalArgumentException("unsupportred type for key " + key);
            }

        } finally {
            l.pop(1);
        }
    }

    private Entity<Object> getEntity(final LuaState l) {

        final int top = l.getTop();

        try {

            l.getField(1, "entity");
            if (l.isNil(-1)) return null;

            final Object entity;
            final String mediaType;

            l.getField(2, "media_type");
            mediaType = l.toString(-1);
            l.pop(1);

            l.getField(2, "value");
            entity = l.toJavaObject(-1, Object.class);
            l.pop(1);

            if (mediaType == null) throw new IllegalArgumentException("Content type must be specified with entity.");
            return Entity.entity(entity, mediaType);

        } finally {
            l.setTop(top);
        }

    }

    private void getOptionalMultiField(final LuaState l, final String key, final BiConsumer<String, String> consumer) {

        final int top = l .getTop();

        try {

            l.getField(1, key);

            if (l.isNil(-1)) return;
            if (!l.isTable( -1)) throw new IllegalArgumentException(key + " must be a table");

            l.pushNil();
            while (l.next(-2)) {

                l.pushValue(-2);
                l.pushValue(-2);

                if (l.isString(-1) || l.isNumber(-1)) {
                    final String h = l.toString(-2);
                    final String v = l.toString(-1);
                    consumer.accept(h, v);
                } else if (l.isTable(-1)) {
                    final String h = l.toString(-2);
                    Stream.of(l.toJavaObject(-1, String[].class))
                          .forEach(v -> consumer.accept(h, v));
                } else {
                    throw new IllegalArgumentException(key + " has invalid value at " + l.toString(-1));
                }

                l.pop(3);

            }

        } finally {
            l.setTop(top);
        }

    }

    private <T> T getOptionalMultiField(final LuaState l, final String key,
                                        final T initial, final BiFunction<String, String[], T> consumer) {

        T out = initial;
        final int top = l .getTop();

        try {

            l.getField(1, key);

            if (l.isNil(-1)) return out;
            if (!l.isTable( -1)) throw new IllegalArgumentException(key + " must be a table");

            l.pushNil();
            while (l.next(-1)) {

                l.pushValue(-2);
                l.pushValue(-2);

                if (l.isString(-1) || l.isNumber(-1)) {
                    out = consumer.apply(l.toString(-2), new String[]{l.toString(-1)});
                } else if (l.isTable(-1)) {
                    out = consumer.apply(l.toString(-2), l.toJavaObject(-1, String[].class));
                } else {
                    throw new IllegalArgumentException(key + " has invalid value at " + l.toString(-1));
                }

                l.pop(3);

            }

            return out;

        } finally {
            l.setTop(top);
        }

    }

    @Override
    public Module getModuleNamed(final String moduleName) {
        return new Module() {
            @Override
            public String getChunkName() {
                return MODULE_NAME;
            }

            @Override
            public boolean exists() {
                return MODULE_NAME.equals(moduleName);
            }
        };
    }

    @Override
    public JavaFunction getLoader() {
        return l -> {
            l.newTable();
            l.pushJavaFunction(send);
            l.setField(-2, SEND);
            return 1;
        };
    }

    @Override
    public void makePersistenceAware(final Persistence persistence) {
        persistence.addPermanentJavaObject(send, HttpClientBuiltin.class, SEND);
    }

    public Client getClient() {
        return client;
    }

    @Inject
    public void setClient(Client client) {
        this.client = client;
    }

    public Context getContext() {
        return context;
    }

    @Inject
    public void setContext(Context context) {
        this.context = context;
    }

}
