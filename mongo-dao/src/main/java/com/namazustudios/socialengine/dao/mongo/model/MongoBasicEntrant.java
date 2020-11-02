package com.namazustudios.socialengine.dao.mongo.model;

import org.bson.types.ObjectId;
import dev.morphia.annotations.*;

import java.util.Date;
import java.util.Map;

/**
 * Created by patricktwohig on 3/26/15.
 */
@Entity(value = "entrant")
public class MongoBasicEntrant {

    @Id
    private ObjectId objectId;

    @Property
    private String salutation;

    @Property
    private String firstName;

    @Property
    private String lastName;

    @Property
    @Indexed(options = @IndexOptions(unique = true))
    private String email;

    @Property
    private Date birthday;

    @Reference
    private Map<String, MongoShortLink> shortLinksByCampaign;

    public ObjectId getObjectId() {
        return objectId;
    }

    public void setObjectId(ObjectId objectId) {
        this.objectId = objectId;
    }

    public String getSalutation() {
        return salutation;
    }

    public void setSalutation(String salutation) {
        this.salutation = salutation;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public Map<String, MongoShortLink> getShortLinksByCampaign() {
        return shortLinksByCampaign;
    }

    public void setShortLinksByCampaign(Map<String, MongoShortLink> shortLinksByCampaign) {
        this.shortLinksByCampaign = shortLinksByCampaign;
    }

}
