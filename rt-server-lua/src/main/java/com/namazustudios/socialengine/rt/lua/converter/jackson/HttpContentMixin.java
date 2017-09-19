package com.namazustudios.socialengine.rt.lua.converter.jackson;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public interface HttpContentMixin {

    @JsonProperty("static_headers")
    Map<String, String> getStaticHeaders();

}
