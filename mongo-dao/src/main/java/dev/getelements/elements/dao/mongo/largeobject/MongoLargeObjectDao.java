package dev.getelements.elements.dao.mongo.largeobject;

import dev.getelements.elements.dao.LargeObjectDao;
import dev.getelements.elements.model.largeobject.LargeObject;

import java.util.Optional;

public class MongoLargeObjectDao implements LargeObjectDao {

    @Override
    public Optional<LargeObject> findLargeObject(final String objectId) {
        return Optional.empty();
    }

    @Override
    public LargeObject createLargeObject(final LargeObject largeObject) {
        throw new UnsupportedOperationException();
    }

    @Override
    public LargeObject updateLargeObject(final LargeObject largeObject) {
        throw new UnsupportedOperationException();
    }

    @Override
    public LargeObject deleteLargeObject(final String objectId) {
        throw new UnsupportedOperationException();
    }

}
