package com.namazustudios.socialengine.dao.mongo.provider;

import com.namazustudios.elements.fts.concurrent.Condition;
import com.namazustudios.elements.fts.concurrent.jeromq.DynamicJeroMQBindStrategy;
import com.namazustudios.elements.fts.concurrent.jeromq.JeroMQBindStrategy;
import com.namazustudios.elements.fts.concurrent.jeromq.JeroMQCondition;
import com.namazustudios.elements.fts.concurrent.jeromq.JeroMQConditionBuilder;
import com.namazustudios.elements.fts.concurrent.jeromq.StaticJeroMQBindStrategy;
import com.namazustudios.socialengine.rt.util.ShutdownHooks;
import org.zeromq.ZContext;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;

public class JeroMQConditionProvider implements Provider<Condition> {

    public static final String BIND_ADDRESS = "com.namazustudios.socialengine.fts.jeromq.condition.bind.address";

    public static final String HOST_ADDRESS = "com.namazustudios.socialengine.fts.jeromq.condition.host.address";

    public static final String BIND_STRATEGY = "com.namazustudios.socialengine.fts.jeromq.condition.bind.strategy";

    private String hostAddresses;

    private String bindAddresses;

    private BindStrategy bindStrategy;

    private Provider<ZContext> zContextProvider;

    private final ShutdownHooks shutdownHooks = new ShutdownHooks(JeroMQConditionProvider.class);

    @Override
    public Condition get() {

        final List<String> hostAddresses = stream(getHostAddresses().split(","))
            .map(s -> s.trim())
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toList());

        final JeroMQConditionBuilder jeroMQConditionBuilder = new JeroMQConditionBuilder();

        switch (getBindStrategy()) {
            case STATIC:
                jeroMQConditionBuilder.withStaticBindStrategy(b -> {
                    b.withBindAddress(getBindAddresses())
                     .withHostAddresses(hostAddresses);
                });
                break;
            case DYNAMIC:
                jeroMQConditionBuilder.withDynamicBindStrategy(b -> b.withPortSpecBinding(getBindAddresses(), hostAddresses));
                break;
            default:
                throw new IllegalStateException("Invalid bind strategy: " + getBindStrategy());
        }

        final JeroMQCondition jeroMQCondition =  jeroMQConditionBuilder
            .build(getzContextProvider().get());

        shutdownHooks.add(jeroMQCondition, () -> jeroMQCondition.close());

        return jeroMQCondition;

    }

    public String getHostAddresses() {
        return hostAddresses;
    }

    @Inject
    public void setHostAddresses(@Named(HOST_ADDRESS) String hostAddresses) {
        this.hostAddresses = hostAddresses;
    }

    public String getBindAddresses() {
        return bindAddresses;
    }

    public BindStrategy getBindStrategy() {
        return bindStrategy;
    }

    @Inject
    public void setBindStrategy(@Named(BIND_STRATEGY) BindStrategy bindStrategy) {
        this.bindStrategy = bindStrategy;
    }

    @Inject
    public void setBindAddresses(@Named(BIND_ADDRESS) String bindAddresses) {
        this.bindAddresses = bindAddresses;
    }

    public Provider<ZContext> getzContextProvider() {
        return zContextProvider;
    }

    @Inject
    public void setzContextProvider(Provider<ZContext> zContextProvider) {
        this.zContextProvider = zContextProvider;
    }

    /**
     * Specifies the {@link JeroMQBindStrategy} to use when configuring the {@link JeroMQCondition};
     */
    public enum BindStrategy {

        /**
         * Indicates that the {@link StaticJeroMQBindStrategy} should be used.
         */
        STATIC,

        /**
         * Indicates that the {@link DynamicJeroMQBindStrategy} should be used.
         */
        DYNAMIC

    }



}
