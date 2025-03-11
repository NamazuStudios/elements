package dev.getelements.elements.rt.remote;

import dev.getelements.elements.sdk.cluster.id.InstanceId;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;

public class PersistentInstanceIdProvider implements Provider<InstanceId> {

    public static final String INSTANCE_ID_FILE = "dev.getelements.elements.rt.id.instance.id.file";

    private String instanceIdFilePath;

    @Override
    public InstanceId get() {
        return InstanceId.loadOrGenerate(getInstanceIdFilePath());
    }

    public String getInstanceIdFilePath() {
        return instanceIdFilePath;
    }

    @Inject
    public void setInstanceIdFilePath(@Named(INSTANCE_ID_FILE) String instanceIdFilePath) {
        this.instanceIdFilePath = instanceIdFilePath;
    }

}
