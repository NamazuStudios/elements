package dev.getelements.elements.dao.mongo.largeobject;

import com.mongodb.MongoGridFSException;
import com.mongodb.MongoWriteException;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.GridFSUploadStream;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import dev.getelements.elements.sdk.dao.LargeObjectBucket;
import dev.getelements.elements.sdk.dao.LargeObjectDao;
import dev.getelements.elements.sdk.model.exception.DuplicateException;
import dev.getelements.elements.sdk.model.exception.InternalException;
import dev.getelements.elements.sdk.model.exception.largeobject.LargeObjectContentNotFoundException;
import dev.getelements.elements.sdk.model.largeobject.LargeObject;
import dev.getelements.elements.sdk.model.largeobject.LargeObjectState;
import dev.getelements.elements.sdk.cluster.path.Path;
import org.bson.BsonString;

import jakarta.inject.Inject;
import org.bson.Document;

import java.io.OutputStream;

import static com.mongodb.client.model.Filters.eq;
import static java.util.Objects.isNull;

public class GridFSLargeObjectBucket implements LargeObjectBucket {

    private GridFSBucket gridFSBucket;

    private LargeObjectDao largeObjectDao;

    @Override
    public OutputStream writeObject(final String objectId) {

        final var largeObject = getLargeObjectDao().getLargeObject(objectId);
        if (exist(objectId)) {
            deleteLargeObject(objectId);
        }

        final var gridFsFileId = new BsonString(objectId);
        final var normalized = new Path(largeObject.getPath()).toPathWithoutContext();

        try {
            final Document metadata = new Document()
                    .append("originalFilename", largeObject.getOriginalFilename())
                    .append("mimeType", largeObject.getMimeType());

            final GridFSUploadOptions options = new GridFSUploadOptions()
                    .metadata(metadata);

            final GridFSUploadStream stream = getGridFSBucket().openUploadStream(
                    gridFsFileId,
                    normalized.toNormalizedPathString(),
                    options
            );

            setUploaded(largeObject);
            return stream;
        } catch (MongoWriteException ex) {
            if (ex.getCode() == 11000) {
                throw new DuplicateException("Contents already exists: " + objectId);
            } else {
                throw new InternalException(ex);
            }
        }
    }

    @Override
    public GridFSDownloadStream readObject(final String objectId) {

        final var largeObject = getLargeObjectDao().getLargeObject(objectId);
        final var gridFsFileId = new BsonString(largeObject.getId());

        try{
            return getGridFSBucket().openDownloadStream(gridFsFileId);
        } catch (MongoGridFSException ex) {
            throw new LargeObjectContentNotFoundException(ex);
        }
    }

    @Override
    public void deleteLargeObject(final String objectId) {
        final var gridFsFileId = new BsonString(objectId);
        getGridFSBucket().delete(gridFsFileId);
    }

    @Override
    public void setUploaded(LargeObject largeObject) {
        largeObject.setState(LargeObjectState.UPLOADED);
        largeObjectDao.updateLargeObject(largeObject);
    }

    public boolean exist(final String objectId) {
        return !isNull(getGridFSBucket().find(eq("_id", objectId)).first());
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
