package dev.getelements.elements.rt.lua.guice.rest;

import java.util.Objects;

public class SimpleModel {

    private String id;

    private String hello;

    private String world;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHello() {
        return hello;
    }

    public void setHello(String hello) {
        this.hello = hello;
    }

    public String getWorld() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof SimpleModel)) return false;
        SimpleModel that = (SimpleModel) object;
        return Objects.equals(getId(), that.getId()) &&
                Objects.equals(getHello(), that.getHello()) &&
                Objects.equals(getWorld(), that.getWorld());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getHello(), getWorld());
    }

}
