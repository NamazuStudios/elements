package com.namazustudios.promotion.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by patricktwohig on 3/25/15.
 */
public class Pagination<T> {

    private int offset;

    private int total;

    private List<T> objects = new ArrayList<>();

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<T> getObjects() {
        return objects;
    }

    public void setObjects(List<T> objects) {
        this.objects = objects;
    }

}
