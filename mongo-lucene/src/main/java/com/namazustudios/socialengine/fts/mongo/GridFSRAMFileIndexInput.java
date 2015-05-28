package com.namazustudios.socialengine.fts.mongo;

import com.mongodb.gridfs.GridFSDBFile;
import org.apache.lucene.store.RAMFile;
import org.apache.lucene.store.RAMInputStream;

import java.io.IOException;

/**
 * Created by patricktwohig on 5/22/15.
 */
public class GridFSRAMFileIndexInput extends RAMInputStream {

    private final int BUFFER_SIZE = 4096;

    private static RAMFile build(final GridFSDBFile gridFsDbFile) {
        final RAMFile ramFile = new RAMFile();
        return ramFile;
    }

    public GridFSRAMFileIndexInput(String name, RAMFile f) throws IOException {
        super(name, f);
    }



}
