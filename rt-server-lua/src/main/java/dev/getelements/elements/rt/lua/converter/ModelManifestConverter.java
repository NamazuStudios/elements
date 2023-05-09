package dev.getelements.elements.rt.lua.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.getelements.elements.rt.lua.converter.jackson.ModelManifestJacksonModule;
import dev.getelements.elements.rt.manifest.model.ModelManifest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by patricktwohig on 8/18/17.
 */
public class ModelManifestConverter extends AbstractMapConverter<ModelManifest> {

    private final ObjectMapper objectMapper;
    {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new ModelManifestJacksonModule());
    }


    @Override
    public Class<ModelManifest> getConvertedType() {
        return ModelManifest.class;
    }

    @Override
    public ModelManifest convertLua2Java(Map<?, ?> map) {

        final Map<String, Object> manifestMap = new HashMap<>();
        manifestMap.put("modelsByName", map);

        final ModelManifest modelManifest = objectMapper.convertValue(manifestMap, ModelManifest.class);
        modelManifest.getModelsByName().forEach((modelName, model) -> model.setName(modelName));
        return modelManifest;

    }

}
