package dev.getelements.elements.rt.lua.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.getelements.elements.rt.lua.converter.jackson.SecurityManifestJacksonModule;
import dev.getelements.elements.rt.manifest.security.SecurityManifest;

import java.util.Map;

public class SecurityManifestConverter extends AbstractMapConverter<SecurityManifest> {

    private final ObjectMapper objectMapper;
    {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new SecurityManifestJacksonModule());
    }

    @Override
    protected Class<SecurityManifest> getConvertedType() {
        return SecurityManifest.class;
    }

    @Override
    public SecurityManifest convertLua2Java(Map<?, ?> map) {

        final SecurityManifest securityManifest = objectMapper.convertValue(map, SecurityManifest.class);

        if (securityManifest.getHeaderAuthSchemesByName() != null) {
            securityManifest.getHeaderAuthSchemesByName().forEach((k, v) -> v.setName(k));
        }

        return securityManifest;

    }

}
