package dev.getelements.elements.dao.mongo.largeobject;

import com.mongodb.DB;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.gridfs.GridFS;
import dev.getelements.elements.dao.LargeObjectBucket;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;

public class MongoLargeObjectBucket implements LargeObjectBucket {

    private DB db;
    private final GridFS gridFS;

    public MongoLargeObjectBucket() {
        assert getDb() != null;
        gridFS = new GridFS(getDb());
    }

    @Override
    public String saveImage(File file) {
//        try {
//            gridFS.createFile(file);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
        return "saved file Url";
    }

    public DB getDb() {
        return db;
    }

    public void setDb(DB db) {
        this.db = db;
    }
}
