package com.namazustudios.socialengine.rt.lua.converter.jackson;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.namazustudios.socialengine.rt.manifest.http.HttpContent;

import java.util.List;
import java.util.Map;

public interface HttpOperationMixin {

    String getName();

    @JsonProperty("auth")
    List<String> getAuthSchemes();

    @JsonProperty("produces")
    Map<String, HttpContent> getProducesContentByType();

    @JsonProperty("consumes")
    Map<String, HttpContent> getConsumesContentByType();

}
