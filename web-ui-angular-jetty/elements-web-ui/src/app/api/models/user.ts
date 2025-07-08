/* tslint:disable */
export interface User {
  id?: string;
  name: string;
  email: string;
  level: 'UNPRIVILEGED' | 'USER' | 'SUPERUSER';
  active?: boolean;
  linkedAccounts?: string[];
}
