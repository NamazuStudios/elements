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
   * The category for the application configuration.
   */
  category: 'MATCHMAKING' | 'PSN_PS4' | 'PSN_VITA' | 'IOS_APP_STORE' | 'ANDROID_GOOGLE_PLAY' | 'FACEBOOK' | 'FIREBASE';

  /**
   * The application-configuration specific uinique ID.  (Varies by ConfigurationCategory)
   */
  uniqueIdentifier?: string;

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
