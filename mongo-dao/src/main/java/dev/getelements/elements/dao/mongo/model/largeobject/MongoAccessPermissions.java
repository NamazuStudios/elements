package dev.getelements.elements.dao.mongo.model.largeobject;

import dev.getelements.elements.sdk.model.largeobject.Subjects;
import dev.morphia.annotations.Embedded;
import dev.morphia.annotations.Property;

import java.util.Objects;

@Embedded
public class MongoAccessPermissions {

    @Property
    private Subjects read;

    @Property
    private Subjects write;

    public Subjects getRead() {
        return read;
    }

    public void setRead(Subjects read) {
        this.read = read;
    }

    public Subjects getWrite() {
        return write;
    }

    public void setWrite(Subjects write) {
        this.write = write;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MongoAccessPermissions that = (MongoAccessPermissions) o;
        return Objects.equals(read, that.read) && Objects.equals(write, that.write);
    }

    @Override
    public int hashCode() {
        return Objects.hash(read, write);
    }
}
