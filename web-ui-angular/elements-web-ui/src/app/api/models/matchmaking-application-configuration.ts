/* tslint:disable */
import { Application } from './application';
import { CallbackDefinition } from './callback-definition';

/**
 * This configures the matchmaking system.  More specifically, this configures which scripts andmethods will be called when a successful match has been made.
 */
export interface MatchmakingApplicationConfiguration {

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
   * A user-sepecified unqiue identifier for the matching scheme.  It is possible to specify multiple schemes per application, but each must be uniquely named.  Each scheme allows for the specification of different scripts to handle the successful match.  When requesting matchmaking services clients will specify the scheme to be used.
   */
  scheme: string;

  /**
   * Specifies the matching algorithm to use.  Algorithms are builtin and implemented by the API services.  Currently, only FIFO is supported.
   */
  algorithm: 'FIFO';

  /**
   * Specifies the callback to execute when a successful match has been made.  When invoked, the method will receive Match object generated as the result of the matchmaking process.  Match instances will easily
   */
  success: CallbackDefinition;
}
