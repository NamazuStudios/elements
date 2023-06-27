import { TokenDefinition } from './token-definition';

/* tslint:disable */
export interface PaginationTokenDefinition {
  offset?: number;

  total?: number;

  approximation?: boolean;

  objects?: TokenDefinition[];
}
