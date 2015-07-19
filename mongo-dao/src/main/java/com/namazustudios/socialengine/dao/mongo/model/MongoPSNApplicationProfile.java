package com.namazustudios.socialengine.dao.mongo.model;

import com.namazustudios.socialengine.fts.annotation.SearchableDocument;
import com.namazustudios.socialengine.fts.annotation.SearchableField;
import com.namazustudios.socialengine.fts.annotation.SearchableIdentity;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.application.Platform;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

/**
 * Created by patricktwohig on 7/10/15.
 */
@SearchableDocument(
        fields = {
                @SearchableField(name = "name", path = "/npIdentifier"),
                @SearchableField(name = "npIdentifier", path = "/npIdentifier")
        }
)
@Entity(value = "application_profile", noClassnameStored = true)
public class MongoPSNApplicationProfile extends AbstractMongoApplicationProfile {

    @Property("name")
    @Indexed(unique = true)
    private String npIdentifier;

    @Property("client_secret")
    private String clientSecret;

    public String getNpIdentifier() {
        return npIdentifier;
    }

    public void setNpIdentifier(String npIdentifier) {
        this.npIdentifier = npIdentifier;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

}
