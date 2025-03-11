/* tslint:disable */
import {Oauth2RequestKeyValue} from "./oauth2-request-key-value";

/**
 * Represents a request to create an Auth Scheme for an Application.
 */
export interface CreateOauth2AuthSchemeRequest {

  /**
   * A unique name used to identify the scheme within the instance of Elements.
   * If using the same OAuth2 provider (e.g. Steam), it is recommended to suffix the name for each application
   * when using multitenancy, e.g. steam_game1, steam_game2, etc.
   */
  name: string;

  /**
   * The URL to send the user token validation request to.
   */
  validationUrl: string;

  /**
   * The headers required for the validation request.
   */
  headers: Array<Oauth2RequestKeyValue>;

  /**
   * The query parameters required for the validation request.
   */
  params: Array<Oauth2RequestKeyValue>;

  /**
   * Determines how to map the user id in the response. For example "response.params.steamid"
   */
  responseIdMapping: string;
}
