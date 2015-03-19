package com.namazustudios.promotion.model;

import java.util.List;

/**
 *
 * Represents a particular promotion within the system.
 *
 * Created by patricktwohig on 3/18/15.
 */
public class Promotion {

    private String name;

    private List<PromotionType> types;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<PromotionType> getTypes() {
        return types;
    }

    public void setTypes(List<PromotionType> types) {
        this.types = types;
    }

    public enum PromotionType {

        /**
         * Enables basic promotional tools.
         */
        BASIC,

        /**
         * Enables Steam specific promotional tools
         */
        STEAM

    }

}
