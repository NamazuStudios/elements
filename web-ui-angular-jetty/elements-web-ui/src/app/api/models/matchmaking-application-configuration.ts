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
   * Specifies the callback to execute when a successful match has been made.  When invoked, the method will receive Match object generated as the result of the matchmaking process.  Match instances will easily
   */
  success: CallbackDefinition;

}
