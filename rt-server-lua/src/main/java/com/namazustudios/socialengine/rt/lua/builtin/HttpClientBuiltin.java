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
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.namazustudios.socialengine.rt.lua.builtin.BuiltinUtils.currentTaskId;
import static javax.ws.rs.HttpMethod.POST;
import static javax.ws.rs.HttpMethod.PUT;

public class HttpClientBuiltin implements Builtin {

    private static final Logger logger = LoggerFactory.getLogger(HttpClientBuiltin.class);

    public static final String MODULE_NAME = "namazu.http.client";

    public static final String SEND = "send";

    private Client client;

    private Context context;

    private final JavaFunction send = l -> {

        final TaskId taskId = currentTaskId(l);

        final String base = getRequiredStringField(l, "base");
        final WebTarget target = getClient().target(base);

        getRequiredStringField(l, "path", target::path);
        getOptionalMultiField(l, "params", target::queryParam);
        getOptionalMultiField(l, "matrix", target::matrixParam);

        final Invocation.Builder builder = target.request();
        getOptionalMultiField(l, "headers", builder::header);
        getOptionalMultiField(l, "cookies", builder::cookie);
        getOptionalStringFields(l, "accept", builder::accept);
        getOptionalStringFields(l, "accept_language", builder::acceptLanguage);

        final String method = getRequiredStringField(l, "method");
        final Entity<Object> entity = getEntity(l);

        if ((PUT.equals(method) || POST.equals(method)) && (entity != null)) {
            builder.async().method(method, entity, new InvocationCallback<Object>() {

                @Override
                public void completed(final Object response) {
                    // TODO Provide Response to the method.
                    logger.info("Got Response {}", response);
                }

                @Override
                public void failed(Throwable throwable) {
                    context.getSchedulerContext().resumeWithError(taskId, throwable);
                }

            });
        } else {
            builder.async().method(method, new InvocationCallback<Object>() {

                @Override
                public void completed(final Object response) {
                    // TODO Provide Response to the method.
                    logger.info("Got Response {}", response);
                }

                @Override
                public void failed(Throwable throwable) {
                    context.getSchedulerContext().resumeWithError(taskId, throwable);
                }

            });
        }

        return 0;

    };

    private String getRequiredStringField(final LuaState l, final String key) {
        try {
            final String s;
            l.getField(1, key);
            if (l.isNil( -1)) throw new IllegalArgumentException(key + " must be specified ");
            return l.toString(-1);
        } finally {
            l.pop(1);
        }
    }

    private void getRequiredStringField(final LuaState l, final String key, final Consumer<String> consumer) {
        try {
            final String s;
            l.getField(1, key);
            if (l.isNil( -1)) throw new IllegalArgumentException(key + " must be specified ");
            consumer.accept(l.toString(-1));
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
            l.setTable(top);
        }
    }

    private void getOptionalObjectField(final LuaState l, final String key, final Consumer<Object> consumer) {
        try {
            final String s;
            l.getField(1, key);
            if (!l.isNil( -1)) consumer.accept(l.toJavaObject(-1, Object.class));
        } finally {
            l.pop(1);
        }
    }

    private void getOptionalMultiField(final LuaState l, final String key, final BiConsumer<String, String> consumer) {

        final int top = l .getTop();

        try {
            l.getField(1, key);

            if (l.isNil(-1)) return;
            if (!l.isTable( -1)) throw new IllegalArgumentException(key + " must be a table");

            l.pushNil();
            while (l.next(-1)) {

                // Copy key/value to avoid wrecking hte string conversion
                l.pushValue(-2);
                l.pushValue(-2);

                if (l.isString(-1) || l.isNumber(-1)) {
                    consumer.accept(l.toString(-2), l.toString(-1));
                } else if (l.isTable(-1)) {
                    final String k = l.toString(-1);
                    Stream.of(l.toJavaObject(-1, String[].class)).forEach(v -> consumer.accept(k, v));
                } else {
                    throw new IllegalArgumentException(key + " has invalid value at " + l.toString(-1));
                }

                l.pop(3);

            }

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
