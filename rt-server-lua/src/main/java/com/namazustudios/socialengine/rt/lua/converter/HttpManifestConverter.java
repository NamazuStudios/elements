package com.namazustudios.socialengine.rt.lua.converter;

import com.namazustudios.socialengine.rt.manifest.http.HttpManifest;

import java.util.Map;

/**
 * Created by patricktwohig on 8/18/17.
 */
public class HttpManifestConverter extends AbstractMapConverter<HttpManifest> {

    @Override
    public Class<HttpManifest> getConvertedType() {
        return HttpManifest.class;
    }

    @Override
    protected HttpManifest convertLua2Java(Map<?, ?> map) {
        return super.convertLua2Java(map);
    }

}
