package com.namazustudios.socialengine.rt.lua.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.namazustudios.socialengine.rt.lua.converter.jackson.StartupManifestJacksonModule;
import com.namazustudios.socialengine.rt.manifest.startup.StartupManifest;

import java.util.HashMap;
import java.util.Map;

public class StartupManifestConverter extends AbstractMapConverter<StartupManifest> {

    private final ObjectMapper objectMapper;
    {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new StartupManifestJacksonModule());
    }

    @Override
    protected Class<StartupManifest> getConvertedType() {
        return StartupManifest.class;
    }

    @Override
    public StartupManifest convertLua2Java(Map<?, ?> map) {
        final Map<String, Object> manifestMap = new HashMap<>();
        manifestMap.put("modulesByName", map);

        final StartupManifest startupManifest = objectMapper.convertValue(manifestMap, StartupManifest.class);

        startupManifest.getModulesByName().forEach((moduleName, module) -> module.setModule(moduleName));
        startupManifest.getModulesByName()
                .values()
                .stream()
                .flatMap(module -> module.getOperationsByName().entrySet().stream())
                .forEach(e -> e.getValue().setName(e.getKey()));

        return startupManifest;

    }

}
