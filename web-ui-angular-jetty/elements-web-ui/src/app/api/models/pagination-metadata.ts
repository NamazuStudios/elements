import { Metadata } from './metadata-tab';

/* tslint:disable */
export interface PaginationMetadata {
  offset?: number;

  total?: number;

  approximation?: boolean;

  objects?: Metadata[];
}
