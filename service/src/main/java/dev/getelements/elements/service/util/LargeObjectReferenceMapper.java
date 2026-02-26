package dev.getelements.elements.service.util;

import dev.getelements.elements.sdk.model.largeobject.LargeObject;
import dev.getelements.elements.sdk.model.largeobject.LargeObjectReference;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import org.mapstruct.Mapper;

@Mapper
public interface LargeObjectReferenceMapper extends MapperRegistry.Mapper<LargeObject, LargeObjectReference> {

    @Override
    LargeObjectReference forward(final LargeObject source);

}
