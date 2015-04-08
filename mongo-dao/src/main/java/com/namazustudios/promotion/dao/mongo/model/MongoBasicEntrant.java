package com.namazustudios.promotion.dao.mongo.model;

import org.mongodb.morphia.Key;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Reference;

import java.util.Date;
import java.util.Map;

/**
 * Created by patricktwohig on 3/26/15.
 */
@Entity(value = "entrant", noClassnameStored = true)
public class MongoBasicEntrant {

    @Id
    private Object objectId;

    @Property("salutation")
    private String salutation;

    @Property("first_name")
    private String firstName;

    @Property("last_name")
    private String lastName;

    @Property("email")
    @Indexed(unique = true)
    private String email;

    @Property("birthday")
    private Date birthday;

    @Reference("short_links_by_campaign")
    private Map<String, MongoShortLink> shortLinksByCampaign;

    public Object getObjectId() {
        return objectId;
    }

    public void setObjectId(Object objectId) {
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
