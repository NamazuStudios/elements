package dev.getelements.elements.rt.lua.converter.jackson;

import com.fasterxml.jackson.databind.module.SimpleModule;
import dev.getelements.elements.rt.manifest.model.Type;
import dev.getelements.elements.rt.manifest.security.SecurityManifest;

public class SecurityManifestJacksonModule extends SimpleModule {

    public SecurityManifestJacksonModule() {
        addSerializer(Type.class, new TypeSerializer());
        addDeserializer(Type.class, new TypeDeserializer());
        setMixInAnnotation(SecurityManifest.class, SecurityManifestMixin.class);
    }

}
