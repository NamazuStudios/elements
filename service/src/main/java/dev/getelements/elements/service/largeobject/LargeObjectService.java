package dev.getelements.elements.service.largeobject;

import dev.getelements.elements.model.largeobject.CreateLargeObjectRequest;
import dev.getelements.elements.model.largeobject.LargeObject;
import dev.getelements.elements.model.largeobject.UpdateLargeObjectRequest;

public interface LargeObjectService {

    LargeObject createLargeObject(CreateLargeObjectRequest objectRequest);

    LargeObject updateLargeObject(UpdateLargeObjectRequest objectRequest);

    LargeObject getLargeObject(String objectId);

    void deleteLargeObject(String objectId);
}
