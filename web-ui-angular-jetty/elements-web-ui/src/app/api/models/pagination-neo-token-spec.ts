import { TokenTemplate } from './token-spec-tab';

/* tslint:disable */
export interface PaginationNeoTokenSpec {
  offset?: number;

  total?: number;

  approximation?: boolean;

  objects?: TokenTemplate[];
}
