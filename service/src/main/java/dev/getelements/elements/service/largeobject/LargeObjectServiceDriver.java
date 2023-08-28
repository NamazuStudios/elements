package dev.getelements.elements.service.largeobject;

import dev.getelements.elements.dao.LargeObjectBucket;

import javax.inject.Inject;
import java.io.File;
import java.io.InputStream;

class LargeObjectServiceDriver {

    private LargeObjectBucket bucket;

    public String createObject(InputStream uploadedInputStream, String fileName) {
        bucket.saveImage(uploadedInputStream, fileName);
        return fileName;
    }

    public LargeObjectBucket getBucket() {
        return bucket;
    }

    @Inject
    public void setBucket(LargeObjectBucket bucket) {
        this.bucket = bucket;
    }
}
