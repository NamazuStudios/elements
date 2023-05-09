package dev.getelements.elements.rt.lua.converter.jackson;

import com.fasterxml.jackson.databind.module.SimpleModule;
import dev.getelements.elements.rt.manifest.http.HttpContent;
import dev.getelements.elements.rt.manifest.http.HttpModule;
import dev.getelements.elements.rt.manifest.http.HttpOperation;
import dev.getelements.elements.rt.manifest.model.Type;
import dev.getelements.elements.rt.manifest.startup.StartupModule;

public class StartupManifestJacksonModule extends SimpleModule {

    public StartupManifestJacksonModule() {
        addSerializer(Type.class, new TypeSerializer());
        addDeserializer(Type.class, new TypeDeserializer());
        setMixInAnnotation(StartupModule.class, StartupModuleMixin.class);
    }

}
