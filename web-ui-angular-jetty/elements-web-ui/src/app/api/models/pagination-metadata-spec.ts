import { MetadataSpec } from './token-spec-tab';

/* tslint:disable */
export interface PaginationMetadataSpec {
  offset?: number;

  total?: number;

  approximation?: boolean;

  objects?: MetadataSpec[];
}
