import { NEP6Contract } from "./nep-6-contract";

/* tslint:disable */
export interface NEP6Account {
  readonly address?: string;

  readonly label?: string;

  readonly isDefault: boolean;

  readonly lock: boolean;

  readonly key: string;

  contract: NEP6Contract;

  extra: any;
}
