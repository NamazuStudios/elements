package dev.getelements.elements.sdk.model.user;

/** Lifecycle status for an identity verification on a {@link UserUid}. */
public enum VerificationStatus {

    /** The UID has not been verified. This is the default state for newly created email UIDs. */
    UNVERIFIED,

    /** A verification request has been initiated; the user has been sent a verification token. */
    PENDING,

    /** The UID has been verified, either by the external provider or by completing the verification flow. */
    VERIFIED

}
