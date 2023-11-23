package dev.getelements.elements.service.largeobject;

import dev.getelements.elements.dao.LargeObjectBucket;
import dev.getelements.elements.dao.LargeObjectDao;
import dev.getelements.elements.exception.ForbiddenException;
import dev.getelements.elements.model.largeobject.CreateLargeObjectFromUrlRequest;
import dev.getelements.elements.model.largeobject.CreateLargeObjectRequest;
import dev.getelements.elements.model.largeobject.LargeObject;
import dev.getelements.elements.model.largeobject.UpdateLargeObjectRequest;
import dev.getelements.elements.service.LargeObjectService;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;

import static java.util.Objects.isNull;

public class AnonLargeObjectService implements LargeObjectService {

    private LargeObjectDao largeObjectDao;

    private LargeObjectBucket largeObjectBucket;

    @Override
    public Optional<LargeObject> findLargeObject(final String objectId) {
        return getLargeObjectDao().findLargeObject(objectId);
    }

    @Override
    public LargeObject updateLargeObject(
            final String objectId,
            final UpdateLargeObjectRequest objectRequest) {
        throw new ForbiddenException();
    }

    @Override
    public LargeObject createLargeObject(final CreateLargeObjectRequest createLargeObjectRequest) {
        throw new ForbiddenException();
    }

    @Override
    public LargeObject createLargeObjectFromUrl(final CreateLargeObjectFromUrlRequest createRequest) {
        throw new ForbiddenException();
    }

    @Override
    public void deleteLargeObject(final String objectId) {
        throw new ForbiddenException();
    }

    @Override
    public InputStream readLargeObjectContent(final String objectId) throws IOException {

        final var largeObject = getLargeObject(objectId);

        if (largeObject.getAccessPermissions().getRead().isWildcard()) {
            return getLargeObjectBucket().readObject(largeObject.getId());
        }

        throw new ForbiddenException();

    }

    @Override
    public OutputStream writeLargeObjectContent(String objectId) throws IOException {
        throw new ForbiddenException();
    }

    @Override
    public LargeObject saveOrUpdateLargeObject(LargeObject largeObject) {
        throw new ForbiddenException();
    }

    public LargeObjectDao getLargeObjectDao() {
        return largeObjectDao;
    }

    @Inject
    public void setLargeObjectDao(LargeObjectDao largeObjectDao) {
        this.largeObjectDao = largeObjectDao;
    }

    public LargeObjectBucket getLargeObjectBucket() {
        return largeObjectBucket;
    }

    @Inject
    public void setLargeObjectBucket(LargeObjectBucket largeObjectBucket) {
        this.largeObjectBucket = largeObjectBucket;
    }

}
