/* tslint:disable */
export interface AuthScheme {
  /**
   * The unique ID of the auth scheme.
   */
  id: string;

  /**
   * A unique name used to identify the scheme within the instance of Elements.
   */
  audience: string;

  /**
   * A base-64 encoded string representing an x509 encoded public key.
pattern: ^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$
   */
publicKey: string;

  /**
   * The digital signature matching the public key format.
   */
  algorithm:
    | 'RSA_256'
    | 'RSA_384'
    | 'RSA_512'
    | 'ECDSA_256'
    | 'ECDSA_384'
    | 'ECDSA_512';

  /**
   * The highest permitted user level this particular scheme will authorize.
   */
  userLevel: 'UNPRIVILEGED' | 'USER' | 'SUPERUSER';

  /**
   * description:The tags used to tag this auth scheme.
   */
  tags: Array<string>;

  /**
   * description:A list of issuers allowed to use this scheme.
   */
  allowedIssuers: Array<string>;
}
