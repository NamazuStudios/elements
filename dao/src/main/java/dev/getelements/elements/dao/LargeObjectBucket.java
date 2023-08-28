package dev.getelements.elements.dao;

import java.io.InputStream;

public interface LargeObjectBucket {

    InputStream readImage(String fileId);

    void saveImage(InputStream inputStream, String fileName);
}
