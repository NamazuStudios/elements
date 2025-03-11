export interface Pagination<T> {
  offset?: number;
  total?: number;
  approximation?: boolean;
  objects?: T[];
}
