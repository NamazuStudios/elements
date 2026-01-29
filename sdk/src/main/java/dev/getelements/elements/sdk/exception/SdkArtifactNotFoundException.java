package dev.getelements.elements.sdk.exception;

public class SdkArtifactNotFoundException extends SdkException {

  public SdkArtifactNotFoundException() {
  }

  public SdkArtifactNotFoundException(String message) {
    super(message);
  }

  public SdkArtifactNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public SdkArtifactNotFoundException(Throwable cause) {
    super(cause);
  }

  public SdkArtifactNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
