package com.namazustudios.socialengine.rt.lua.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.namazustudios.socialengine.rt.lua.converter.jackson.HttpManifestJacksonModule;
import com.namazustudios.socialengine.rt.manifest.http.HttpManifest;

import java.util.HashMap;
import java.util.Map;

import static java.util.stream.Stream.concat;

/**
 * Created by patricktwohig on 8/18/17.
 */
public class HttpManifestConverter extends AbstractMapConverter<HttpManifest> {

    private final ObjectMapper objectMapper;
    {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new HttpManifestJacksonModule());
    }

    @Override
    public Class<HttpManifest> getConvertedType() {
        return HttpManifest.class;
    }

    @Override
    public HttpManifest convertLua2Java(Map<?, ?> map) {

        final Map<String, Object> manifestMap = new HashMap<>();
        manifestMap.put("modulesByName", map);

        final HttpManifest httpManifest = objectMapper.convertValue(manifestMap, HttpManifest.class);

        httpManifest.getModulesByName().forEach((moduleName, module) -> module.setModule(moduleName));
        httpManifest.getModulesByName()
            .values()
            .stream()
            .flatMap(module -> module.getOperationsByName().entrySet().stream())
            .forEach(e -> e.getValue().setName(e.getKey()));

        httpManifest.getModulesByName()
            .values()
            .stream()
            .flatMap(module -> module.getOperationsByName().values().stream())
            .flatMap(operation -> concat(operation.getConsumesContentByType().entrySet().stream(),
                                         operation.getProducesContentByType().entrySet().stream()))
            .forEach(e -> {e.getValue().setType(e.getKey()); e.getValue().setPayloadType(Map.class);} );

        return httpManifest;

    }


}
