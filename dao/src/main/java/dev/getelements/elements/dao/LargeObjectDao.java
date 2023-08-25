package dev.getelements.elements.dao;

import dev.getelements.elements.model.largeobject.LargeObject;

import java.util.Optional;

public interface LargeObjectDao {

    void createOrUpdateLargeObject(LargeObject largeObject);
    LargeObject getLargeObject(String objectId);
    Optional<LargeObject> findLargeObject(String objectId);
    void deleteLargeObject(String objectId);
}
