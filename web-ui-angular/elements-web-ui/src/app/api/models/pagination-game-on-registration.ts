/* tslint:disable */
import { GameOnRegistration } from './game-on-registration';
export interface PaginationGameOnRegistration {
  offset?: number;
  total?: number;
  approximation?: boolean;
  objects?: Array<GameOnRegistration>;
}
