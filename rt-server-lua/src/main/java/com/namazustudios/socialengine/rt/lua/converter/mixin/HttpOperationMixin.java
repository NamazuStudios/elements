package com.namazustudios.socialengine.rt.lua.converter.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.namazustudios.socialengine.rt.manifest.http.HttpContent;

import java.util.Map;

@JsonRootName("name")
public interface HttpOperationMixin {

    @JsonProperty("produces")
    Map<String, HttpContent> getProducesContentByType();

    @JsonProperty("consumes")
    Map<String, HttpContent> getConsumesContentByType();

}
