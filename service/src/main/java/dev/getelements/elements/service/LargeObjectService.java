package dev.getelements.elements.service;

import dev.getelements.elements.model.largeobject.CreateLargeObjectRequest;
import dev.getelements.elements.model.largeobject.LargeObject;
import dev.getelements.elements.model.largeobject.UpdateLargeObjectRequest;
import dev.getelements.elements.rt.annotation.DeprecationDefinition;
import dev.getelements.elements.rt.annotation.Expose;
import dev.getelements.elements.rt.annotation.ExposedBindingAnnotation;
import dev.getelements.elements.rt.annotation.ModuleDefinition;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

@Expose({
        @ModuleDefinition(
                value = "eci.elements.service.largeobject"
        ),
        @ModuleDefinition(
                value = "eci.elements.service.unscoped.largeobject",
                annotation = @ExposedBindingAnnotation(Unscoped.class)
        ),
        @ModuleDefinition(
                value = "namazu.elements.service.largeobject",
                deprecated = @DeprecationDefinition("Use eci.elements.service.largeobject instead.")
        ),
        @ModuleDefinition(
                value = "namazu.elements.service.unscoped.largeobject",
                annotation = @ExposedBindingAnnotation(Unscoped.class),
                deprecated = @DeprecationDefinition("Use eci.elements.service.unscoped.largeobject instead.")
        )
})
public interface LargeObjectService {

    LargeObject getLargeObject(String objectId);


    LargeObject updateLargeObject(UpdateLargeObjectRequest objectRequest);

    void deleteLargeObject(String objectId);

    OutputStream writeLargeObjectStream(String objectId);

    WritableByteChannel writeLargeObjectChannel(String objectId);

    InputStream readLargeObjectStream(String objectId);

    ReadableByteChannel readLargeObjectChannel(String objectId);

    LargeObject createLargeObject(InputStream uploadedInputStream, String fileName);
}
