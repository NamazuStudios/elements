/* tslint:disable */
/**
 * Represents a response from creating an Auth Scheme for an Application.
 */
export interface CreateCustomAuthSchemeResponse {
  /**
   * The full JSON response as described in AuthScheme
   */
  scheme?: string;
  /**
   * The Base64 public key that was either given or generated during creation. See https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/security/spec/X509EncodedKeySpec.html for details on the specifics of the format.
   */
  publicKey: string;
  /**
   * The Base64 public key that was either given or generated during creation. See https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/security/spec/PKCS8EncodedKeySpec.html for details on the specifics of the format.
   */
  privateKey?: string;
}
