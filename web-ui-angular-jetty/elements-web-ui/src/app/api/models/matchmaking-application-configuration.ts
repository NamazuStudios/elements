/* tslint:disable */
import { Application } from './application';
import { CallbackDefinition } from './callback-definition';
import {ElementServiceReference} from "./element-service-reference";

/**
 * This configures the matchmaking system.  More specifically, this configures which scripts and methods will be called
 * when a successful match has been made.
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

  /**
   * The Element Service Reference for the application.
   */
  matchmaker: ElementServiceReference;

  /**
   * The maximum nubmer of profiles in the match.  This is used to limit the size of the match and prevent runaway
   * matches.
   */
  maxProfiles: number;

  /**
   * The amount of time the match will linger after it has been ended.  This allows for any final processing to be done
   * on the match before it is deleted.
   */
  lingerSeconds: number;

  /**
   * The amount of time to wait for a match to exist before it will be automatically abandoned by the system.
   */
  timeoutSeconds: number;

}
