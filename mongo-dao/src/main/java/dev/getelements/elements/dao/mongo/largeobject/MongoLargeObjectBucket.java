package dev.getelements.elements.dao.mongo.largeobject;

import com.mongodb.DB;
import com.mongodb.MongoException;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.gridfs.GridFS;
import dev.getelements.elements.dao.LargeObjectBucket;
import dev.getelements.elements.exception.NotFoundException;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MongoLargeObjectBucket implements LargeObjectBucket {

    private GridFSBucket gridFSBucket;

//    @Override
//    public InputStream readFile(String path) {
//        try {
//            return getGridFSBucket().openDownloadStream(path);
//        } catch (MongoException ex) {
//            throw new NotFoundException(ex);
//        }
//
//    }

    public GridFSBucket getGridFSBucket() {
        return gridFSBucket;
    }

    @Inject
    public void setGridFSBucket(final GridFSBucket gridFSBucket) {
        this.gridFSBucket = gridFSBucket;
    }

    @Override
    public void saveImage(InputStream inputStream, String fileName) {
        try {
            getGridFSBucket().openUploadStream(fileName).write(inputStream.readAllBytes());
        } catch (MongoException ex) {
            throw new NotFoundException(ex);
        } catch (IOException ex) {
            throw new NotFoundException(ex);
        }
    }
}
