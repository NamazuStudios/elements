/* tslint:disable */
import { Application } from './application';

/**
 * Used to create a mock session with the server.  This will create a temporary user as well which will exist for a short period of time
 */
export interface MockSessionRequest {

  /**
   * The lifetime of the user in seconds.  After this amount of time,
   */
  lifetimeInSeconds?: number;

  /**
   * The Application to use when creating the associated user profile.  If null, then no profile will be generated.
   */
  application?: Application;
}
