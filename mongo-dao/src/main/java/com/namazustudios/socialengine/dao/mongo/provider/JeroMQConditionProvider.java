package com.namazustudios.socialengine.dao.mongo.provider;

import com.namazustudios.elements.fts.concurrent.Condition;
import com.namazustudios.elements.fts.concurrent.jeromq.JeroMQCondition;
import com.namazustudios.elements.fts.concurrent.jeromq.JeroMQConditionBuilder;
import com.namazustudios.socialengine.util.ShutdownHooks;
import org.zeromq.ZContext;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import static java.util.Arrays.stream;

public class JeroMQConditionProvider implements Provider<Condition> {

    public static final String BIND_ADDRESS = "com.namazustudios.socialengine.fts.jeromq.condition.bind.address";

    public static final String HOST_ADDRESS = "com.namazustudios.socialengine.fts.jeromq.condition.host.address";

    public static final String PORT_RANGE_LOWER = "com.namazustudios.socialengine.fts.jeromq.condition.lower.port";

    public static final String PORT_RANGE_UPPER = "com.namazustudios.socialengine.fts.jeromq.condition.upper.port";

    private int lowerPortRange;

    private int upperPortRange;

    private String hostAddresses;

    private String bindAddresses;

    private Provider<ZContext> zContextProvider;

    private final ShutdownHooks shutdownHooks = new ShutdownHooks(JeroMQConditionProvider.class);

    @Override
    public Condition get() {

        final JeroMQConditionBuilder builder = new JeroMQConditionBuilder();

        builder.withDynamicBindStrategy(b -> {
            b.withPortRange(getLowerPortRange(), getUpperPortRange())
             .withBinding(getBindAddresses(), getHostAddresses());
        });

        final JeroMQCondition jeroMQCondition =  new JeroMQConditionBuilder().build(getzContextProvider().get());
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

    public int getLowerPortRange() {
        return lowerPortRange;
    }

    @Inject
    public void setLowerPortRange(@Named(PORT_RANGE_LOWER) int lowerPortRange) {
        this.lowerPortRange = lowerPortRange;
    }

    public int getUpperPortRange() {
        return upperPortRange;
    }

    @Inject
    public void setUpperPortRange(@Named(PORT_RANGE_UPPER) int upperPortRange) {
        this.upperPortRange = upperPortRange;
    }

}
