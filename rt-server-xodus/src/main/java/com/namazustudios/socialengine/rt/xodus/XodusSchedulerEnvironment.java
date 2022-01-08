package com.namazustudios.socialengine.rt.xodus;

import com.namazustudios.socialengine.rt.SchedulerEnvironment;
import com.namazustudios.socialengine.rt.util.ProxyDelegate;
import jetbrains.exodus.env.Environment;
import jetbrains.exodus.env.Environments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;

public class XodusSchedulerEnvironment implements SchedulerEnvironment {

    public static final String SCHEDULER_ENVIRONMENT = "com.namazustudios.socialengine.rt.xodus.scheduler";

    private static final Logger logger = LoggerFactory.getLogger(XodusSchedulerEnvironment.class);

    public static final String SCHEDULER_ENVIRONMENT_PATH = "com.namazustudios.socialengine.rt.xodus.scheduler.path";

    private String environmentPath;

    private ProxyDelegate<Environment> environment;

    @Override
    public void start() {
        final var path = getEnvironmentPath();
        logger.info("Opening Xodus environment for Scheduled Tasks at {}", path);
        getEnvironment().start(() -> Environments.newInstance(path));
    }

    @Override
    public void stop() {
        getEnvironment().stop().close();
    }

    public String getEnvironmentPath() {
        return environmentPath;
    }

    @Inject
    public void setEnvironmentPath(@Named(SCHEDULER_ENVIRONMENT_PATH) String environmentPath) {
        this.environmentPath = environmentPath;
    }

    public ProxyDelegate<Environment> getEnvironment() {
        return environment;
    }

    @Inject
    public void setEnvironment(@Named(SCHEDULER_ENVIRONMENT) ProxyDelegate<Environment> environment) {
        this.environment = environment;
    }

}
