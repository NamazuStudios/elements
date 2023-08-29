package dev.getelements.elements.dao.mongo.largeobject;

import com.mongodb.MongoGridFSException;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import dev.getelements.elements.dao.LargeObjectBucket;
import dev.getelements.elements.dao.LargeObjectDao;
import dev.getelements.elements.exception.NotFoundException;
import dev.getelements.elements.exception.largeobject.LargeObjectContentNotFoundException;

import javax.inject.Inject;
import java.io.OutputStream;

public class GridFSLargeObjectBucket implements LargeObjectBucket {

    private GridFSBucket gridFSBucket;

    private LargeObjectDao largeObjectDao;

    @Override
    public void deleteLargeObject(final String objectId) {

        final var deleted = getLargeObjectDao().deleteLargeObject(objectId);

        try (final var inputStream = getGridFSBucket().openDownloadStream(deleted.getId())) {
            final var bsonValueObjectId = inputStream.getGridFSFile().getId();
            getGridFSBucket().delete(bsonValueObjectId);
        }

    }

    @Override
    public GridFSDownloadStream readObject(final String objectId) {

        final var largeObject = getLargeObjectDao().getLargeObject(objectId);

        try{
            return getGridFSBucket().openDownloadStream(largeObject.getPath());
        } catch (MongoGridFSException ex) {
            throw new LargeObjectContentNotFoundException(ex);
        }

    }

    @Override
    public OutputStream writeObject(final String objectId) {

        final var largeObject = getLargeObjectDao().getLargeObject(objectId);

        try {
            return getGridFSBucket().openUploadStream(largeObject.getPath());
        } catch (MongoGridFSException ex) {
            throw new NotFoundException(ex);
        }

    }

    public LargeObjectDao getLargeObjectDao() {
        return largeObjectDao;
    }

    @Inject
    public void setLargeObjectDao(LargeObjectDao largeObjectDao) {
        this.largeObjectDao = largeObjectDao;
    }

    public GridFSBucket getGridFSBucket() {
        return gridFSBucket;
    }

    @Inject
    public void setGridFSBucket(GridFSBucket gridFSBucket) {
        this.gridFSBucket = gridFSBucket;
    }

}
