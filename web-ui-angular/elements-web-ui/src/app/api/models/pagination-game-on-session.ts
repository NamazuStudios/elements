/* tslint:disable */
import { GameOnSession } from './game-on-session';
export interface PaginationGameOnSession {
  offset?: number;
  total?: number;
  approximation?: boolean;
  objects?: Array<GameOnSession>;
}
