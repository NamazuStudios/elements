package com.namazustudios.socialengine.rt.xodus;

import com.namazustudios.socialengine.rt.exception.ResourceNotFoundException;
import com.namazustudios.socialengine.rt.id.ResourceId;
import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.env.Cursor;
import jetbrains.exodus.env.Transaction;

import java.util.Iterator;

public class XodusResourceBlock {

    private final ByteIterable key;

    private final ByteIterable value;

    public XodusResourceBlock(final ByteIterable key, final ByteIterable value) {
        this.key = key;
        this.value = value;
    }

    public ByteIterable getKey() {
        return key;
    }

    public ByteIterable getValue() {
        return value;
    }

}
