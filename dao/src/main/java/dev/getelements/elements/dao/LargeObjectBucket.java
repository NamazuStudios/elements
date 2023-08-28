package dev.getelements.elements.dao;

import java.io.InputStream;

public interface LargeObjectBucket {

    void saveImage(InputStream inputStream, String fileName);
}
