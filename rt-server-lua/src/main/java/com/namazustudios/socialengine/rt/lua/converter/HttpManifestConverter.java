package com.namazustudios.socialengine.rt.lua.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.namazustudios.socialengine.rt.lua.converter.mixin.HttpContentMixin;
import com.namazustudios.socialengine.rt.lua.converter.mixin.HttpModuleMixin;
import com.namazustudios.socialengine.rt.lua.converter.mixin.HttpOperationMixin;
import com.namazustudios.socialengine.rt.lua.converter.mixin.TypeDeserializer;
import com.namazustudios.socialengine.rt.manifest.http.HttpContent;
import com.namazustudios.socialengine.rt.manifest.http.HttpManifest;
import com.namazustudios.socialengine.rt.manifest.http.HttpModule;
import com.namazustudios.socialengine.rt.manifest.http.HttpOperation;
import com.namazustudios.socialengine.rt.manifest.model.Type;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by patricktwohig on 8/18/17.
 */
public class HttpManifestConverter extends AbstractMapConverter<HttpManifest> {

    /**
     * The name of the lua module specified in the lua code.  Corresponds to the value
     * of {@link HttpModule#getModule()}.
     */
    public static final String MODULE_KEY = "module";

    /**
     * The name of the lua module specified in the lua code.  Corresponds to the value
     * of {@link HttpModule#getOperationsByName()}.
     */
    public static final String OPERATIONS_KEY = "operations";

    public static final String VERB_KEY = "verb";

    public static final String PATH_KEY = "path";

    public static final String METHOD_KEY = "method";

    public static final String PRODUCES_KEY = "produces";

    public static final String CONSUMES_KEY = "consumes";

    public static final String HEADERS_KEY = "headers";

    public static final String MODEL_KEY = "model";

    public static final String PARAMETERS_KEY = "parameters";

    public static final String DESCRIPTION_KEY = "description";

    public static final String STATIC_HEADERS = "static_headers";

    @Override
    public Class<HttpManifest> getConvertedType() {
        return HttpManifest.class;
    }

    @Override
    public HttpManifest convertLua2Java(Map<?, ?> map) {

        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.addMixIn(HttpModule.class, HttpModuleMixin.class);
        objectMapper.addMixIn(HttpOperation.class, HttpOperationMixin.class);
        objectMapper.addMixIn(HttpContent.class, HttpContentMixin.class);

        final SimpleModule typeDeserializerModule = new SimpleModule();
        typeDeserializerModule.addDeserializer(Type.class, new TypeDeserializer());
        objectMapper.registerModule(typeDeserializerModule);

        final Map<String, Object> manifestMap = new HashMap<>();
        manifestMap.put("modulesByName", map);

        return objectMapper.convertValue(manifestMap, HttpManifest.class);

    }

}