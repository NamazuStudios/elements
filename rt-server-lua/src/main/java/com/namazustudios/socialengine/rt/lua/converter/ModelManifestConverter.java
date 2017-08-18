package com.namazustudios.socialengine.rt.lua.converter;

import com.namazustudios.socialengine.rt.manifest.model.ModelManifest;

import java.util.Map;

/**
 * Created by patricktwohig on 8/18/17.
 */
public class ModelManifestConverter extends AbstractMapConverter<ModelManifest> {

    @Override
    public Class<ModelManifest> getConvertedType() {
        return ModelManifest.class;
    }

    @Override
    protected ModelManifest convertLua2Java(Map<?, ?> map) {
        return super.convertLua2Java(map);
    }

}
