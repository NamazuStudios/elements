package com.namazustudios.socialengine.rt.lua.converter.jackson;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.namazustudios.socialengine.rt.manifest.model.Type;

public class ModelManifestJacksonModule extends SimpleModule {

    public ModelManifestJacksonModule() {
        addSerializer(Type.class, new TypeSerializer());
        addDeserializer(Type.class, new TypeDeserializer());
    }

}
