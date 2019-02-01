package com.namazustudios.socialengine.rt.lua.converter.jackson;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.namazustudios.socialengine.rt.manifest.http.HttpOperation;
import com.namazustudios.socialengine.rt.manifest.startup.StartupOperation;

import java.util.Map;

public interface  StartupModuleMixin {

    String getModule();

    @JsonProperty("operations")
    Map<String, StartupOperation> getOperationsByName();
}
