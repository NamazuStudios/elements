/* tslint:disable */
import { User } from './user';
import { Profile } from './profile';
import { Application } from './application';
export interface Session {
  user: User;
  profile?: Profile;
  application?: Application;
  expiry?: number;
}
