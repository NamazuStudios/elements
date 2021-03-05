package com.namazustudios.socialengine.rt.remote;

import com.namazustudios.socialengine.rt.Persistence;
import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.exception.InvalidInstanceIdException;
import com.namazustudios.socialengine.rt.id.InstanceId;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.io.*;

import static com.namazustudios.socialengine.rt.id.InstanceId.randomInstanceId;
import static java.io.File.createTempFile;

public class PersistentInstanceIdProvider implements Provider<InstanceId> {

    public static final String INSTANCE_ID_FILE = "com.namazustudios.socialengine.rt.id.instance.id.file";

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
