package dev.getelements.elements.dao.mongo.model;

import dev.getelements.elements.sdk.model.user.User;
import dev.morphia.annotations.*;
import org.bson.types.ObjectId;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

/**
 * Created by patricktwohig on 3/31/15.
 */
@Entity(value = "user", useDiscriminator = false)
@Indexes({
        @Index(
                fields = {@Field("linkedAccounts")},
                options = @IndexOptions(
                        partialFilter = "{ linkedAccounts: { $exists: true } }"
                )
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

    @Property
    private Set<String> linkedAccounts;

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

    public Set<String> getLinkedAccounts() {
        return linkedAccounts;
    }

    public void setLinkedAccounts(Set<String> linkedAccounts) {
        this.linkedAccounts = linkedAccounts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MongoUser mongoUser = (MongoUser) o;
        return Objects.equals(objectId, mongoUser.objectId) && Objects.equals(name, mongoUser.name) && Objects.equals(primaryPhoneNb, mongoUser.primaryPhoneNb) && Objects.equals(firstName, mongoUser.firstName) && Objects.equals(lastName, mongoUser.lastName) && Objects.equals(email, mongoUser.email) && Objects.equals(hashAlgorithm, mongoUser.hashAlgorithm) && Arrays.equals(salt, mongoUser.salt) && Arrays.equals(passwordHash, mongoUser.passwordHash) && level == mongoUser.level && Objects.equals(linkedAccounts, mongoUser.linkedAccounts);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(objectId, name, primaryPhoneNb, firstName, lastName, email, hashAlgorithm, level, linkedAccounts);
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
                ", linkedAccounts=" + linkedAccounts +
                '}';
    }
}
