/* tslint:disable */
import { Application } from './application';

/**
 * Houses the various parameters required which allow communication with the Faceook API.  The Facebook API will
 */
export interface FacebookApplicationConfiguration {

  /**
   * The database assigned ID for the application configuration.
   */
  id?: string;

  /**
   * The application-configuration specific uinique ID.  (Varies by ConfigurationCategory)
   */
  name: string;

  /**
   * The category for the application configuration.
   */
  type: string;

  /**
   * A description of the application configuration.
   */
  description: string;

  /**
   * The parent application owning this configuration.
   */
  parent: Application;

  /**
   * The AppID as it appears in the Facebook Developer Console
   */
  applicationId: string;

  /**
   * The App Secret as it appears in the Facebook Developer Console
   */
  applicationSecret: string;

  /**
   * The set of built-in permissions connected clients will need to request.
   */
  builtinApplicationPermissions?: Array<string>;

}
