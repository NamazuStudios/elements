package com.namazustudios.socialengine.rt.lua.converter.jackson;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.namazustudios.socialengine.rt.manifest.http.HttpContent;
import com.namazustudios.socialengine.rt.manifest.http.HttpModule;
import com.namazustudios.socialengine.rt.manifest.http.HttpOperation;
import com.namazustudios.socialengine.rt.manifest.model.Type;
import com.namazustudios.socialengine.rt.manifest.startup.StartupModule;

public class StartupManifestJacksonModule extends SimpleModule {

    public StartupManifestJacksonModule() {
        addSerializer(Type.class, new TypeSerializer());
        addDeserializer(Type.class, new TypeDeserializer());
        setMixInAnnotation(StartupModule.class, StartupModuleMixin.class);
    }

}
