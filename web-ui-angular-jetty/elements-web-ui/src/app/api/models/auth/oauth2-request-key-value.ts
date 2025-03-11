/* tslint:disable */
export interface Oauth2RequestKeyValue {

  /**
   * The key.
   */
  key: string;

  /**
   * The value.
   */
  value: string;

  /**
   * If this value should be received from the client, or predefined and stored in the DB.
   */
  fromClient: boolean;
}
