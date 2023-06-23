/* tslint:disable */
import { ApplicationConfiguration } from './application-configuration';
export interface PaginationApplicationConfiguration {
  offset?: number;
  total?: number;
  approximation?: boolean;
  objects?: Array<ApplicationConfiguration>;
}
