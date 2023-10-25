package dev.getelements.elements.dao.mongo.model;

import dev.getelements.elements.model.user.User;
import dev.morphia.annotations.*;
import org.bson.types.ObjectId;

import java.util.Arrays;
import java.util.Objects;

import static dev.morphia.utils.IndexType.TEXT;

/**
 * Created by patricktwohig on 3/31/15.
 */
@Entity(value = "user", useDiscriminator = false)
@Indexes({
    @Index(
        fields = {
            @Field(value = "name", type = TEXT),
            @Field(value = "email", type = TEXT)
        }
    ),
    @Index(
        fields = @Field(value = "name"),
        options = @IndexOptions(unique = true, collation = @Collation(locale = "en_US"))
    ),
    @Index(
        fields = @Field(value = "email"),
        options = @IndexOptions(unique = true, collation = @Collation(locale = "en_US"))
    ),
    @Index(
        fields = @Field(value = "firebaseId"),
        options = @IndexOptions(unique = true, sparse = true)
    ),
    @Index(
        fields = @Field(value = "facebookId"),
        options = @IndexOptions(unique = true, sparse = true)
    ),
    @Index(
        fields = @Field(value = "appleSignInId"),
        options = @IndexOptions(unique = true, sparse = true)
    ),
    @Index(
        fields = @Field(value = "externalUserId"),
        options = @IndexOptions(unique = true, sparse = true)
    )

})
public class MongoUser {

    @Id
    private ObjectId objectId;

    @Property
    private String name;

    @Property
    private String primaryPhoneNb;

    @Property
    private String firstName;

    @Property
    private String lastName;

    @Property
    private String email;

    @Property
    private String hashAlgorithm;

    @Property
    private byte[] salt;

    @Property
    private byte[] passwordHash;

    @Property
    private User.Level level;

    @Indexed
    @Property
    private boolean active;

    @Property
    private String firebaseId;

    @Property
    private String facebookId;

    @Property
    private String appleSignInId;

    @Property
    private String externalUserId;

    public ObjectId getObjectId() {
        return objectId;
    }

    public void setObjectId(ObjectId objectId) {
        this.objectId = objectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getHashAlgorithm() {
        return hashAlgorithm;
    }

    public void setHashAlgorithm(String hashAlgorithm) {
        this.hashAlgorithm = hashAlgorithm;
    }

    public byte[] getSalt() {
        return salt;
    }

    public void setSalt(byte[] salt) {
        this.salt = salt;
    }

    public byte[] getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(byte[] passwordHash) {
        this.passwordHash = passwordHash;
    }

    public User.Level getLevel() {
        return level;
    }

    public void setLevel(User.Level level) {
        this.level = level;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getFirebaseId() {
        return firebaseId;
    }

    public void setFirebaseId(String firebaseId) {
        this.firebaseId = firebaseId;
    }

    public String getFacebookId() {
        return facebookId;
    }

    public void setFacebookId(String facebookId) {
        this.facebookId = facebookId;
    }

    public String getAppleSignInId() {
        return appleSignInId;
    }

    public void setAppleSignInId(String appleSignInId) {
        this.appleSignInId = appleSignInId;
    }

    public String getExternalUserId() {
        return externalUserId;
    }

    public void setExternalUserId(String externalUserId) {
        this.externalUserId = externalUserId;
    }

    public String getPrimaryPhoneNb() {
        return primaryPhoneNb;
    }

    public void setPrimaryPhoneNb(String primaryPhoneNb) {
        this.primaryPhoneNb = primaryPhoneNb;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MongoUser mongoUser = (MongoUser) o;
        return active == mongoUser.active && Objects.equals(objectId, mongoUser.objectId) && Objects.equals(name, mongoUser.name) && Objects.equals(primaryPhoneNb, mongoUser.primaryPhoneNb) && Objects.equals(firstName, mongoUser.firstName) && Objects.equals(lastName, mongoUser.lastName) && Objects.equals(email, mongoUser.email) && Objects.equals(hashAlgorithm, mongoUser.hashAlgorithm) && Arrays.equals(salt, mongoUser.salt) && Arrays.equals(passwordHash, mongoUser.passwordHash) && level == mongoUser.level && Objects.equals(firebaseId, mongoUser.firebaseId) && Objects.equals(facebookId, mongoUser.facebookId) && Objects.equals(appleSignInId, mongoUser.appleSignInId) && Objects.equals(externalUserId, mongoUser.externalUserId);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(objectId, name, primaryPhoneNb, firstName, lastName, email, hashAlgorithm, level, active, firebaseId, facebookId, appleSignInId, externalUserId);
        result = 31 * result + Arrays.hashCode(salt);
        result = 31 * result + Arrays.hashCode(passwordHash);
        return result;
    }

    @Override
    public String toString() {
        return "MongoUser{" +
                "objectId=" + objectId +
                ", name='" + name + '\'' +
                ", primaryPhoneNb='" + primaryPhoneNb + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", hashAlgorithm='" + hashAlgorithm + '\'' +
                ", salt=" + Arrays.toString(salt) +
                ", passwordHash=" + Arrays.toString(passwordHash) +
                ", level=" + level +
                ", active=" + active +
                ", firebaseId='" + firebaseId + '\'' +
                ", facebookId='" + facebookId + '\'' +
                ", appleSignInId='" + appleSignInId + '\'' +
                ", externalUserId='" + externalUserId + '\'' +
                '}';
    }
}
