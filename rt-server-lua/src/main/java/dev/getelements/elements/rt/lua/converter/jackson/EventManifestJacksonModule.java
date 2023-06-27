package dev.getelements.elements.rt.lua.converter.jackson;

import com.fasterxml.jackson.databind.module.SimpleModule;
import dev.getelements.elements.rt.manifest.event.EventOperation;
import dev.getelements.elements.rt.manifest.model.Type;

public class EventManifestJacksonModule extends SimpleModule {

    public EventManifestJacksonModule() {
        addSerializer(Type.class, new TypeSerializer());
        addDeserializer(Type.class, new TypeDeserializer());
        setMixInAnnotation(EventOperation.class, EventOperationMixin.class);
    }
}
