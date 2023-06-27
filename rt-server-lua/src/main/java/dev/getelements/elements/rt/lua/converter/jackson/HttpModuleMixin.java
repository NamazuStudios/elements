package dev.getelements.elements.rt.lua.converter.jackson;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.getelements.elements.rt.manifest.http.HttpOperation;

import java.util.Map;

public interface  HttpModuleMixin {

    String getModule();

    @JsonProperty("operations")
    Map<String, HttpOperation> getOperationsByName();



}
