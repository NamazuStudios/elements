package com.namazustudios.socialengine.rt.jackson;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.namazustudios.socialengine.rt.annotation.CodeStyle;
import com.namazustudios.socialengine.rt.annotation.RemoteModel;
import com.namazustudios.socialengine.rt.annotation.RemoteScope;
import com.namazustudios.socialengine.rt.manifest.model.Model;
import com.namazustudios.socialengine.rt.manifest.model.ModelIntrospector;
import com.namazustudios.socialengine.rt.manifest.model.Property;
import com.namazustudios.socialengine.rt.manifest.model.Type;

import javax.inject.Inject;
import java.util.Map;
import java.util.stream.Collectors;

import static com.namazustudios.socialengine.rt.manifest.model.Type.ARRAY;
import static com.namazustudios.socialengine.rt.manifest.model.Type.OBJECT;
import static java.lang.String.format;

public class JacksonModelIntrospector implements ModelIntrospector {

    private ObjectMapper objectMapper;

    @Override
    public Type introspectClassForType(final Class<?> cls) {
        final var javaType = getObjectMapper().getTypeFactory().constructType(cls);
        return introspectJavaTypeForType(javaType);
    }

    public Type introspectJavaTypeForType(final JavaType javaType) {
        if (javaType.isMapLikeType()) {
            return OBJECT;
        } else if (javaType.isCollectionLikeType()) {
            return ARRAY;
        } else {
            return Type.findByClass(javaType.getRawClass());
        }
    }

    @Override
    public String introspectClassForModelName(final Class<?> cls, final RemoteScope remoteScope) {
        final var typeCaseFormat = remoteScope.style().typeCaseFormat();
        return RemoteModel.Util.findName(cls)
            .map(s -> CodeStyle.JVM_NATIVE.typeCaseFormat().to(typeCaseFormat, s))
            .orElse(null);
    }

    @Override
    public Model introspectClassForModel(final Class<?> cls, final RemoteScope remoteScope) {
        final var model = new Model();
        final var name = introspectClassForModelName(cls, remoteScope);
        final var description = format("Model for Type: %s", cls.getName());
        final var properties = introspectProperties(cls, remoteScope);
        model.setName(name);
        model.setDescription(description);
        model.setProperties(properties);
        return model;
    }

    public Map<String, Property> introspectProperties(final Class<?> cls, final RemoteScope remoteScope) {

        final var javaType = getObjectMapper().getTypeFactory().constructType(cls);
        final var beanDescription = getObjectMapper().getSerializationConfig().introspect(javaType);

        return beanDescription
            .findProperties()
            .stream()
            .map(bpd -> buildProperty(bpd, remoteScope))
            .collect(Collectors.toMap(Property::getName, p -> p));

    }

    private Property buildProperty(final BeanPropertyDefinition beanPropertyDefinition, final RemoteScope remoteScope) {

        final var property = new Property();
        final var javaType = beanPropertyDefinition.getPrimaryType();
        final var type = introspectJavaTypeForType(beanPropertyDefinition.getPrimaryType());

        final var name = CodeStyle.JVM_NATIVE
            .propertyCaseFormat()
            .to(remoteScope.style().propertyCaseFormat(), beanPropertyDefinition.getName());

        property.setType(type);
        property.setName(name);
        property.setDescription(format("Model for: %s", beanPropertyDefinition.getRawPrimaryType()));

        final var contentType = javaType.getContentType();
        if (contentType != null) {
            final var modelName = introspectClassForModelName(contentType.getRawClass(), remoteScope);
            property.setModel(modelName);
        }

        return property;

    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    @Inject
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

}
