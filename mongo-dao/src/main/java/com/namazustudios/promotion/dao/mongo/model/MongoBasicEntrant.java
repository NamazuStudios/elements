package com.namazustudios.promotion.dao.mongo.model;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Reference;

import java.util.Date;

/**
 * Created by patricktwohig on 3/26/15.
 */
@Entity("entrant")
public class MongoBasicEntrant {

    @Id
    public ObjectId oid;

    @Property
    private String salutation;

    @Property
    private String firstName;

    @Property
    private String lastName;

    @Property
    private String email;

    @Property
    private Date birthday;

    @Reference
    private MongoShortLink shortLink;

    public ObjectId getOid() {
        return oid;
    }

    public void setOid(ObjectId oid) {
        this.oid = oid;
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

    public MongoShortLink getShortLink() {
        return shortLink;
    }

    public void setShortLink(MongoShortLink shortLink) {
        this.shortLink = shortLink;
    }

}
