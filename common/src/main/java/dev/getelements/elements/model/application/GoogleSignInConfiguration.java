package dev.getelements.elements.model.application;

import dev.getelements.elements.annotation.PemFile;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class GoogleSignInConfiguration {

    @NotNull
    @Size(min = 10, max = 10)
    private String keyId;

    @NotNull
    @Size(min = 10, max = 10)
    private String teamId;

    @NotNull
    private String clientId;

    @PemFile
    @NotNull
    private String googleSignInPrivateKey;

    /**
     * Gets the 10 character key id, which is assigned when they key is issued.
     * @return
     */
    public String getKeyId() {
        return keyId;
    }

    /**
     * Sets the 10 character key ID, which is assigned when the key is issued
     * @param keyId the 10 character key-id
     */
    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    /**
     * Gets the 10 character team ID.
     *
     * @return
     */
    public String getTeamId() {
        return teamId;
    }

    /**
     * Sets the 10 character team ID.
     *
     * @param teamId the team id
     */
    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    /**
     * Gets the client id.
     *
     * @return
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Sets the client id.
     *
     * @param clientId the client id
     */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     * Gets the Google Sign-In private key.
     *
     * @return the google-sign in private key.
     */
    public String getGoogleSignInPrivateKey() {
        return googleSignInPrivateKey;
    }

    /**
     * Sets the Google Sign-In Private Key
     *
     * @param googleSignInPrivateKey
     */
    public void setGoogleSignInPrivateKey(String googleSignInPrivateKey) {
        this.googleSignInPrivateKey = googleSignInPrivateKey;
    }

}
