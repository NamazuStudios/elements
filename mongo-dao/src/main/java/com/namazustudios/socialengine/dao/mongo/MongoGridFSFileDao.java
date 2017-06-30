package com.namazustudios.socialengine.dao.mongo;

import com.mongodb.MongoException;
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

    private GridFS gridFS;

    @Override
    public InputStream readFile(String path) {

        final GridFSDBFile gridFSDBFile;

        try {
            gridFSDBFile = getGridFS().findOne(path);
        } catch (MongoException ex) {
            throw new NotFoundException(ex);
        }

        return gridFSDBFile.getInputStream();

    }

    @Override
    public OutputStream writeFile(String path) {

        final GridFSInputFile gridFSDBFile;

        try {
            gridFSDBFile = getGridFS().createFile(path);
        } catch (MongoException ex) {
            throw new NotFoundException(ex);
        }

        return gridFSDBFile.getOutputStream();

    }

    public GridFS getGridFS() {
        return gridFS;
    }

    @Inject
    public void setGridFS(final GridFS gridFS) {
        this.gridFS = gridFS;
    }

}
