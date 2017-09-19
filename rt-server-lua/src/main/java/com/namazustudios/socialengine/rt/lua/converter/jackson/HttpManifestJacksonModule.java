package com.namazustudios.socialengine.rt.lua.converter.jackson;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.namazustudios.socialengine.rt.manifest.http.HttpContent;
import com.namazustudios.socialengine.rt.manifest.http.HttpModule;
import com.namazustudios.socialengine.rt.manifest.http.HttpOperation;
import com.namazustudios.socialengine.rt.manifest.model.Type;

public class HttpManifestJacksonModule extends SimpleModule {

    public HttpManifestJacksonModule() {
        addSerializer(Type.class, new TypeSerializer());
        addDeserializer(Type.class, new TypeDeserializer());
        setMixInAnnotation(HttpModule.class, HttpModuleMixin.class);
        setMixInAnnotation(HttpOperation.class, HttpOperationMixin.class);
        setMixInAnnotation(HttpContent.class, HttpContentMixin.class);
    }

}
