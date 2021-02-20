package com.namazustudios.socialengine.test;

import com.namazustudios.socialengine.rt.IocResolver;
import com.namazustudios.socialengine.rt.Subscription;
import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.remote.Instance;

import java.util.function.Consumer;

public interface EmbeddedInstanceContainer extends AutoCloseable {

    Instance getInstance();

    InstanceId getInstanceId();

    IocResolver getIocResolver();

    EmbeddedInstanceContainer start();

    Subscription onClose(Consumer<? super EmbeddedInstanceContainer> consumer);

    void close();

}
