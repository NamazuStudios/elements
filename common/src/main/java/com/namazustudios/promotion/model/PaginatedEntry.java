package com.namazustudios.promotion.model;

import java.util.List;

/**
 * Created by patricktwohig on 3/25/15.
 */
public class PaginatedEntry<T> {

    private int offset;

    private int count;

    private List<T> objects;

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<T> getObjects() {
        return objects;
    }

    public void setObjects(List<T> objects) {
        this.objects = objects;
    }

}
