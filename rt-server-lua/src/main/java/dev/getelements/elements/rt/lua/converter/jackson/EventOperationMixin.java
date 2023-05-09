package dev.getelements.elements.rt.lua.converter.jackson;

import com.fasterxml.jackson.annotation.JsonProperty;

public interface EventOperationMixin {

    @JsonProperty
    String getModule();

    @JsonProperty
    String getMethod();
}
