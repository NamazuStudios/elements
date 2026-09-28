package dev.getelements.elements.dao.mongo.model.auth;

import dev.getelements.elements.sdk.model.auth.CaptchaProvider;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Property;
import org.bson.types.ObjectId;

import java.util.Objects;

@Entity(value = "captcha_configuration", useDiscriminator = false)
public class MongoCaptchaConfiguration {

    @Id
    private ObjectId id;

    @Property
    private CaptchaProvider provider = CaptchaProvider.RECAPTCHA;

    @Property
    private String siteKey;

    @Property
    private String secretKey;

    @Property
    private boolean enabled;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public CaptchaProvider getProvider() {
        return provider;
    }

    public void setProvider(CaptchaProvider provider) {
        this.provider = provider;
    }

    public String getSiteKey() {
        return siteKey;
    }

    public void setSiteKey(String siteKey) {
        this.siteKey = siteKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MongoCaptchaConfiguration that = (MongoCaptchaConfiguration) o;
        return enabled == that.enabled
                && Objects.equals(id, that.id)
                && provider == that.provider
                && Objects.equals(siteKey, that.siteKey)
                && Objects.equals(secretKey, that.secretKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, provider, siteKey, secretKey, enabled);
    }

}
