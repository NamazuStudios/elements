package com.namazustudios.socialengine.dao.mongo;

import com.mongodb.MongoException;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import com.namazustudios.socialengine.dao.FileDao;
import com.namazustudios.socialengine.exception.NotFoundException;

import javax.inject.Inject;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 *
 * Created by patricktwohig on 6/29/17.
 */
public class MongoGridFSFileDao implements FileDao {

    private GridFSBucket gridFSBucket;

    @Override
    public InputStream readFile(String path) {
        try {
            return getGridFSBucket().openDownloadStream(path);
        } catch (MongoException ex) {
            throw new NotFoundException(ex);
        }

    }

    @Override
    public OutputStream writeFile(String path) {
        try {
            return getGridFSBucket().openUploadStream(path);
        } catch (MongoException ex) {
            throw new NotFoundException(ex);
        }

    }

    public GridFSBucket getGridFSBucket() {
        return gridFSBucket;
    }

    @Inject
    public void setGridFSBucket(final GridFSBucket gridFSBucket) {
        this.gridFSBucket = gridFSBucket;
    }

}
