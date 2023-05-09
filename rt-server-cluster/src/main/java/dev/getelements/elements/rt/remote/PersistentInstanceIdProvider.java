package dev.getelements.elements.rt.remote;

import dev.getelements.elements.rt.id.InstanceId;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import static java.io.File.createTempFile;

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
