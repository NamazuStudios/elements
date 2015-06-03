package com.namazustudios.socialengine.fts;

import com.namazustudios.socialengine.fts.annotation.SearchableDocument;
import com.namazustudios.socialengine.fts.annotation.SearchableField;
import com.namazustudios.socialengine.fts.annotation.SearchableIdentity;

/**
 * Created by patricktwohig on 6/3/15.
 */
@SearchableIdentity(@SearchableField(name = "id", path = "/id", type = String.class))
@SearchableDocument
public class UnrelatedType {

    private String id;

    public UnrelatedType() {}

    public UnrelatedType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
