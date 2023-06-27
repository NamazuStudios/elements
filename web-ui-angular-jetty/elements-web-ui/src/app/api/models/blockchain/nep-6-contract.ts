import { NEP6Parameter } from "./nep-6-parameter";

/* tslint:disable */
export interface NEP6Contract {
  readonly parameters?: NEP6Parameter[];

  readonly script?: string;

  readonly deployed?: boolean;
}
