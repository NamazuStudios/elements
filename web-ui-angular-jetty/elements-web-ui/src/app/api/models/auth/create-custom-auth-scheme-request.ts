/* tslint:disable */
/**
 * Represents a request to create an Auth Scheme for an Application.
 */
export interface CreateCustomAuthSchemeRequest {
  /**
   * The JWT audience for the scheme. Must be unique.
   */
  audience: string;

  /**
   * The Base64 public key that was either given or generated during creation. See https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/security/spec/X509EncodedKeySpec.html for details on the specifics of the format.
pattern: ^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$
   */
  publicKey: string;

  algorithm: 'RSA_256' | 'RSA_384' | 'RSA_512' | 'ECDSA_256' | 'ECDSA_384' | 'ECDSA_512';

  /**
   * The highest permitted user level this particular scheme will authorize.
   */
  userLevel: 'UNPRIVILEGED' | 'USER' | 'SUPERUSER';

  /**
   * description:A list of tags used to index the auth scheme.
   */
  tags: Array<string>;

  /**
   * description:The list of issuers allowed to use this scheme.
   */
  allowedIssuers: Array<string>;
}
