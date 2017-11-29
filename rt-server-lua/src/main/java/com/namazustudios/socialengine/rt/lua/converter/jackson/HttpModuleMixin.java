package com.namazustudios.socialengine.rt.lua.converter.jackson;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.namazustudios.socialengine.rt.manifest.http.HttpOperation;

import java.util.Map;

public interface  HttpModuleMixin {

    String getModule();

    @JsonProperty("operations")
    Map<String, HttpOperation> getOperationsByName();



}
