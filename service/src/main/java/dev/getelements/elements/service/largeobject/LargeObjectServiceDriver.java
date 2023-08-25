package dev.getelements.elements.service.largeobject;

import dev.getelements.elements.dao.LargeObjectBucket;

import javax.inject.Inject;
import java.io.File;

class LargeObjectServiceDriver {

    private LargeObjectBucket bucket;

    public String createObject() {
        return bucket.saveImage(new File("dummy"));
    }

    public LargeObjectBucket getBucket() {
        return bucket;
    }

    @Inject
    public void setBucket(LargeObjectBucket bucket) {
        this.bucket = bucket;
    }
}
