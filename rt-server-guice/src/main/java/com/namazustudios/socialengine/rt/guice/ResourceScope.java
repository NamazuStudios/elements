package com.namazustudios.socialengine.rt.guice;

import com.namazustudios.socialengine.rt.CurrentResource;
import com.namazustudios.socialengine.rt.Resource;

public class ResourceScope {

    private static final ReentrantThreadLocalScope<Resource> instance;

    static {
        instance = new ReentrantThreadLocalScope<Resource>(
            Resource.class,
            CurrentResource.getInstance(),
            resource -> resource.getAttributes()
        );
    }

    public static ReentrantThreadLocalScope<Resource> getInstance() {
        return instance;
    }

}
