/* tslint:disable */

import {ElementServiceReference} from "./element-service-reference";

/**
 * Defines a script method and a module to invoke.
 */
export interface CallbackDefinition {

  /**
   * Specifies the method to invoke.
   */
  method: string;

  /**
   * Specifies the service to use.
   */
  service: ElementServiceReference;

}
