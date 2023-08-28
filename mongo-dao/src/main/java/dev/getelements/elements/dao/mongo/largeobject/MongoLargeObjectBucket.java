package dev.getelements.elements.dao.mongo.largeobject;

import com.mongodb.DB;
import com.mongodb.MongoException;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.GridFSUploadStream;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.mongodb.gridfs.GridFS;
import dev.getelements.elements.dao.LargeObjectBucket;
import dev.getelements.elements.exception.NotFoundException;
import org.bson.Document;
import org.bson.types.ObjectId;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MongoLargeObjectBucket implements LargeObjectBucket {

    private static final String BUCKET_NAME = ("imgBucket");

    private MongoDatabase mongoDatabase;
    private GridFSBucket gridFSBucket;

    @Override
    public GridFSDownloadStream readImage(String fileId) {
        try{
            //tmp check bucket size
            gridFSBucket.find().forEach(ggg -> System.out.println(ggg.getFilename()));

            return gridFSBucket.openDownloadStream(new ObjectId(fileId));
        } catch (MongoException ex) {
            throw new NotFoundException(ex);
        }

    }

    @Override
    public void saveImage(InputStream inputStream, String fileName) {
        try {
//            GridFSUploadOptions options = new GridFSUploadOptions()
//                    .chunkSizeBytes(1024)
//                    .metadata(new Document("type", "presentation"));

            ObjectId fileId = gridFSBucket.uploadFromStream(fileName, inputStream);
            // store this fileId
            System.out.println(fileId);

//            gridFSBucket.openUploadStream(fileName).write(inputStream.readAllBytes());
        } catch (MongoException ex) {
            throw new NotFoundException(ex);
        }
    }

    public MongoDatabase getMongoDatabase() {
        return mongoDatabase;
    }

    @Inject
    public void setMongoDatabase(MongoDatabase mongoDatabase) {
        this.mongoDatabase = mongoDatabase;
        this.gridFSBucket = GridFSBuckets.create(mongoDatabase, BUCKET_NAME);
    }
}
