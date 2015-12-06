package com.namazustudios.socialengine.rt.guice.example.client;

import java.util.List;

/**
 * Created by patricktwohig on 12/4/15.
 */
public class ListClocksResponse {

    private List<ClockMetadata> clocks;

    public List<ClockMetadata> getClocks() {
        return clocks;
    }

    public void setClocks(List<ClockMetadata> clocks) {
        this.clocks = clocks;
    }

}
