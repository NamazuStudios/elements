/* tslint:disable */
import { Application } from './application';

/**
 * Represents the application configuration for Amazon GameOn.  This houses the api keys as well as the public encryption key (if applicable).
 */
export interface GameOnApplicationConfiguration {

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
   * The Amazon Assigned Game ID.  This mirrors the unique identifier of the configuration.
   */
  gameId: string;

  /**
   * The public API key for the application.  This is safe to share with end-users and otherwise untrusted clients.
   */
  publicApiKey: string;

  /**
   * The admin API key for the application.  This is secret and should only be shared with trusted administrator users.
   */
  adminApiKey: string;

  /**
   * The public key for signing requests.  Only required for applications with advanced security.
   */
  publicKey?: string;
}
