package dev.getelements.elements.rt.lua.converter.jackson;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.getelements.elements.rt.manifest.security.AuthScheme;

import java.util.Map;

public interface SecurityManifestMixin {

    @JsonProperty("header")
    Map<String, AuthScheme.Header> getHeaderAuthSchemesByName();

}
