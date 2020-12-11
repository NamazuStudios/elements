package com.namazustudios.socialengine.rt.lua.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.namazustudios.socialengine.rt.lua.converter.jackson.EventManifestJacksonModule;
import com.namazustudios.socialengine.rt.manifest.event.EventManifest;

import java.util.*;

public class EventManifestConverter extends AbstractMapConverter<EventManifest> {

    private final ObjectMapper objectMapper;
    {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new EventManifestJacksonModule());
    }

    @Override
    protected Class<EventManifest> getConvertedType() { return EventManifest.class; }

    @Override
    public EventManifest convertLua2Java(Map<?, ?> map) {
        final Map<String, Object> manifestMap = new HashMap<>();
        manifestMap.put("modulesByEventName", map);

        return objectMapper.convertValue(manifestMap, EventManifest.class);
    }
}
