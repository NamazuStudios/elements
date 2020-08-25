/* tslint:disable */

import { Application } from './application';
import { AppleSignInConfiguration } from './apple-sign-in-configuration'

export interface IosApplicationConfiguration {

  /**
   * The database assigned ID for the application configuration.
   */
  id?: string;

  /**
   * The category for the application configuration.
   */
  category: 'MATCHMAKING' | 'PSN_PS4' | 'PSN_VITA' | 'IOS_APP_STORE' | 'ANDROID_GOOGLE_PLAY' | 'FACEBOOK' | 'FIREBASE' | 'AMAZON_GAME_ON';

  /**
   * The application-configuration specific uinique ID.  (Varies by ConfigurationCategory)
   */
  uniqueIdentifier?: string;

  /**
   * The parent application owning this configuration.
   */
  parent: Application;

  /**
   * The application id.
   */
  applicationId?: string;

  /**
   * The Apple Sign-In configuration.
   */
  appleSignInConfiguration?: AppleSignInConfiguration;

}
