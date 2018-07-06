package com.namazustudios.socialengine.rt.lua.builtin;

import com.namazustudios.socialengine.jnlua.JavaFunction;
import com.namazustudios.socialengine.jnlua.LuaState;
import com.namazustudios.socialengine.rt.TaskId;
import com.namazustudios.socialengine.rt.lua.LuaResource;
import com.namazustudios.socialengine.rt.lua.persist.Persistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.client.WebTarget;
import java.util.concurrent.Future;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.namazustudios.socialengine.rt.lua.builtin.BuiltinUtils.currentTaskId;

public class HttpClientBuiltin implements Builtin {

    private static final Logger logger = LoggerFactory.getLogger(HttpClientBuiltin.class);

    public static final String MODULE_NAME = "namazu.http.client";

    private Client client;

    private final LuaResource luaResource;

    public HttpClientBuiltin(LuaResource luaResource) {
        this.luaResource = luaResource;
    }

    private final JavaFunction send = l -> {

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
        final Invocation invocation = builder.build(method);

        final TaskId taskId = currentTaskId(l);

        final Future<Object> future = invocation.submit(new InvocationCallback<Object>() {

            @Override
            public void completed(final Object object) {
                logger.info("Got object {}", object);

            }

            @Override
            public void failed(final Throwable throwable) {
                luaResource.resumeWithError(taskId, throwable);
            }

        });

        l.pushJavaFunction(_l -> {
            future.cancel(false);
            return 0;
        });

        return 1;

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
        return null;
    }

    @Override
    public void makePersistenceAware(final Persistence persistence) {

    }

    public Client getClient() {
        return client;
    }

    @Inject
    public void setClient(Client client) {
        this.client = client;
    }

}
