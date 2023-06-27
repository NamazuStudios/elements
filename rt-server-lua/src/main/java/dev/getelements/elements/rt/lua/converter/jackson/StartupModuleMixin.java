package dev.getelements.elements.rt.lua.converter.jackson;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.getelements.elements.rt.manifest.http.HttpOperation;
import dev.getelements.elements.rt.manifest.startup.StartupOperation;

import java.util.Map;

public interface  StartupModuleMixin {

    String getModule();

    @JsonProperty("operations")
    Map<String, StartupOperation> getOperationsByName();
}
