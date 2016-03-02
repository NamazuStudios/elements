package com.namazustudios.socialengine.rt.guice.example.client;

import java.util.List;

/**
 * Created by patricktwohig on 12/4/15.
 */
public class ListClocksResponse {

    private String message;

    private List<ClockMetadata> clocks;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<ClockMetadata> getClocks() {
        return clocks;
    }

    public void setClocks(List<ClockMetadata> clocks) {
        this.clocks = clocks;
    }

}
