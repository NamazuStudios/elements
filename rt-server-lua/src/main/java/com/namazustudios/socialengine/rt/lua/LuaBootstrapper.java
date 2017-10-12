package com.namazustudios.socialengine.rt.lua;

import com.google.common.collect.ImmutableMap;
import com.namazustudios.socialengine.rt.Bootstrapper;
import com.namazustudios.socialengine.rt.Path;

import java.io.InputStream;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Created by patricktwohig on 8/22/17.
 */
public class LuaBootstrapper implements Bootstrapper {

    private final Map<Path, Supplier<InputStream>> BOOTSTRAP_RESOURCES;

    {
        BOOTSTRAP_RESOURCES = new ImmutableMap.Builder<Path, Supplier<InputStream>>()
                .put(new Path("main.lua"), () -> getClass().getResourceAsStream("/main.lua"))
                .put(new Path("example/hello_world.lua"), () -> getClass().getResourceAsStream("/example/hello_world.lua"))
            .build();
    }

    @Override
    public Map<Path, Supplier<InputStream>> getBootstrapResources() {
        return BOOTSTRAP_RESOURCES;
    }

}
