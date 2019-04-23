import {Profile} from '../api/models/profile';
import {Application} from '../api/models/application';
import {User} from '../api/models/user';

export class ProfileViewModel implements Profile {
  application: Application;
  displayName: string;
  id: string;
  imageUrl: string;
  user: User;
}
