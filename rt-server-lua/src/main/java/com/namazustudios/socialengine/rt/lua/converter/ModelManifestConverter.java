package com.namazustudios.socialengine.rt.lua.converter;

import com.namazustudios.socialengine.rt.exception.BadManifestException;
import com.namazustudios.socialengine.rt.manifest.model.Model;
import com.namazustudios.socialengine.rt.manifest.model.ModelManifest;
import com.namazustudios.socialengine.rt.manifest.model.Property;
import com.namazustudios.socialengine.rt.manifest.model.Type;

import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.stream;

/**
 * Created by patricktwohig on 8/18/17.
 */
public class ModelManifestConverter extends AbstractMapConverter<ModelManifest> {

    public static final String TYPE_KEY = "type";

    public static final String MODEL_KEY = "model";

    public static final String PROPERTIES_KEY = "properties";

    public static final String DESCRIPTION_KEY = "description";

    @Override
    public Class<ModelManifest> getConvertedType() {
        return ModelManifest.class;
    }

    @Override
    public ModelManifest convertLua2Java(Map<?, ?> map) {
        final Map<String, Model> modelsByName = toModelsByName(map);
        final ModelManifest modelManifest = new ModelManifest();
        modelManifest.setModelsByName(modelsByName);
        return modelManifest;
    }

    private Map<String, Model> toModelsByName(final Map<?, ?> map) {
        final Map<String, Model> modelsByName = new HashMap<>();
        map.forEach((k,v) -> modelsByName.put(k.toString(), toModel(k, v)));
        return modelsByName;
    }

    private Model toModel(Object key, Object value) {

        final String name = Conversion.from(key)
                .asCastTo(String.class)
                .orThrow(v -> new BadManifestException("Got non-string value string for module name: " + key))
                .get();

        final Map<?, Conversion<?>> modelConversionMap = Conversion.fromMap(Conversion.from(value)
                .asCastTo(Map.class)
                .orThrow(v -> new BadManifestException("Got non-table value model definition: " + key))
                .get());

        final String description = modelConversionMap.get(DESCRIPTION_KEY)
                .asCastTo(String.class)
                .orElse("")
                .get();

        final Map propertiesMap = modelConversionMap.get(PROPERTIES_KEY)
                .asCastTo(Map.class)
                .orThrow(v -> new BadManifestException("Got non-table value for properties: " + v ))
                .get();

        final Map<String, Property> propertiesByName = new HashMap<>();
        propertiesMap.forEach((k,v) -> propertiesByName.put(k.toString(), toProperty(k, v)));

        final Model model = new Model();
        model.setName(name);
        model.setDescription(description);
        model.setProperties(propertiesByName);
        return model;

    }

    private Property toProperty(Object key, Object value) {

        final String name = Conversion.from(key)
                .asCastTo(String.class)
                .orThrow(v -> new BadManifestException("Got non-string value for property name: " + v))
                .get();

        final Map<?, Conversion<?>> propertyConversionMap = Conversion.fromMap(Conversion.from(value)
                .asCastTo(Map.class)
                .orThrow(v -> new BadManifestException("Got non-table value property definition: " + key))
                .get());

        final String description = propertyConversionMap.get(DESCRIPTION_KEY)
                .asCastTo(String.class)
                .orElse("")
                .get();

        final Type type = propertyConversionMap.get(TYPE_KEY)
                .asCastTo(String.class)
                .asMappedBy(Type::findByValue, v -> stream(Type.values()).anyMatch(e -> e.value.equals(v)))
                .orThrow(v -> new BadManifestException("Got invalid property type: " + v))
                .get();

        final Property property = new Property();
        property.setName(name);
        property.setDescription(description);
        property.setType(type);

        if (type.equals(Type.ARRAY) || type.equals(Type.OBJECT)) {

            final String model = propertyConversionMap.get(MODEL_KEY)
                .asCastTo(String.class)
                .orThrow(v -> new BadManifestException("Got non-string type for model: " + v))
                .get();

            property.setModel(model);

        }

        return property;

    }

}
