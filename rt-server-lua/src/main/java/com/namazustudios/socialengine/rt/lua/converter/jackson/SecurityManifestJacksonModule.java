package com.namazustudios.socialengine.rt.lua.converter.jackson;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.namazustudios.socialengine.rt.manifest.model.Type;
import com.namazustudios.socialengine.rt.manifest.security.SecurityManifest;

public class SecurityManifestJacksonModule extends SimpleModule {

    public SecurityManifestJacksonModule() {
        addSerializer(Type.class, new TypeSerializer());
        addDeserializer(Type.class, new TypeDeserializer());
        setMixInAnnotation(SecurityManifest.class, SecurityManifestMixin.class);
    }

}
