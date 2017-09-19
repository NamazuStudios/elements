package com.namazustudios.socialengine.rt.lua.converter.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.namazustudios.socialengine.rt.manifest.http.HttpOperation;

import java.util.Map;

@JsonRootName("module")
public interface  HttpModuleMixin {

    String getModule();

    @JsonProperty("operations")
    Map<String, HttpOperation> getOperationsByName();

}
