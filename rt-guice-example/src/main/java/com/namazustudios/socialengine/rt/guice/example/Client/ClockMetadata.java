package com.namazustudios.socialengine.rt.guice.example.client;

/**
 * Created by patricktwohig on 12/4/15.
 */
public class ClockMetadata {

    private String name;

    private String location;

    private String timeZone;

    private String path;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
