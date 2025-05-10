/* tslint:disable */
import { Application } from './application';
export interface ApplicationConfiguration {

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

}
