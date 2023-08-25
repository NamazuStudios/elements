package dev.getelements.elements.dao.mongo;

import dev.getelements.elements.dao.LargeObjectDao;
import dev.getelements.elements.model.largeobject.LargeObject;

import java.util.Optional;

public class MongoLargeObjectDao implements LargeObjectDao {

    @Override
    public void createOrUpdateLargeObject(LargeObject largeObject) {

    }

    @Override
    public LargeObject getLargeObject(String objectId) {
        return null;
    }

    @Override
    public Optional<LargeObject> findLargeObject(String objectId) {
        return Optional.empty();
    }

    @Override
    public void deleteLargeObject(String objectId) {

    }
}
