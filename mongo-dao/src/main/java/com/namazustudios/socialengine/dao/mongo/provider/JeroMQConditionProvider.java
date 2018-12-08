package com.namazustudios.socialengine.dao.mongo.provider;

import com.namazustudios.elements.fts.mongo.Condition;
import com.namazustudios.elements.fts.concurrent.jeromq.JeroMQCondition;
import com.namazustudios.elements.fts.concurrent.jeromq.JeroMQConditionBuilder;
import com.namazustudios.socialengine.util.ShutdownHooks;
import org.zeromq.ZContext;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import static java.util.Arrays.stream;

public class JeroMQConditionProvider implements Provider<Condition> {

    public static final String BIND_ADDRESSES = "com.namazustudios.socialengine.fts.jeromq.condition.bind.addresses";

    public static final String HOST_ADDRESSES = "com.namazustudios.socialengine.fts.jeromq.condition.host.addresses";

    private String hostAddresses;

    private String bindAddresses;

    private Provider<ZContext> zContextProvider;

    private final ShutdownHooks shutdownHooks = new ShutdownHooks(JeroMQConditionProvider.class);

    @Override
    public Condition get() {

        final JeroMQConditionBuilder builder = new JeroMQConditionBuilder();

        stream(getBindAddresses().split(","))
            .map(s -> s.trim())
            .filter(s -> !s.isEmpty())
            .forEach(builder::withBindAddress);

        stream(getHostAddresses().split(","))
            .map(s -> s.trim())
            .filter(s -> !s.isEmpty())
            .forEach(builder::withHostAddress);

        final JeroMQCondition jeroMQCondition =  new JeroMQConditionBuilder().build(getzContextProvider().get());
        shutdownHooks.add(jeroMQCondition, () -> jeroMQCondition.close());

        return jeroMQCondition;

    }

    public String getHostAddresses() {
        return hostAddresses;
    }

    @Inject
    public void setHostAddresses(@Named(HOST_ADDRESSES) String hostAddresses) {
        this.hostAddresses = hostAddresses;
    }

    public String getBindAddresses() {
        return bindAddresses;
    }

    @Inject
    public void setBindAddresses(@Named(BIND_ADDRESSES) String bindAddresses) {
        this.bindAddresses = bindAddresses;
    }

    public Provider<ZContext> getzContextProvider() {
        return zContextProvider;
    }

    @Inject
    public void setzContextProvider(Provider<ZContext> zContextProvider) {
        this.zContextProvider = zContextProvider;
    }

}
