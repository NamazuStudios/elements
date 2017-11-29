package com.namazustudios.socialengine.rt.lua.converter.jackson;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.namazustudios.socialengine.rt.manifest.security.AuthScheme;

import java.util.Map;

public interface SecurityManifestMixin {

    @JsonProperty("header")
    Map<String, AuthScheme.Header> getHeaderAuthSchemesByName();

}
