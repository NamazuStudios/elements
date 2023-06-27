package dev.getelements.elements.rt.lua.converter.jackson;

import com.fasterxml.jackson.databind.module.SimpleModule;
import dev.getelements.elements.rt.manifest.http.HttpContent;
import dev.getelements.elements.rt.manifest.http.HttpModule;
import dev.getelements.elements.rt.manifest.http.HttpOperation;
import dev.getelements.elements.rt.manifest.model.Type;

public class HttpManifestJacksonModule extends SimpleModule {

    public HttpManifestJacksonModule() {
        addSerializer(Type.class, new TypeSerializer());
        addDeserializer(Type.class, new TypeDeserializer());
        setMixInAnnotation(HttpModule.class, HttpModuleMixin.class);
        setMixInAnnotation(HttpOperation.class, HttpOperationMixin.class);
        setMixInAnnotation(HttpContent.class, HttpContentMixin.class);
    }

}
