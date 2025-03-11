import {User} from "../api/models/user";

export class UserViewModel implements User {
  active: boolean;
  email: string;
  facebookId: string;
  id: string;
  level: "UNPRIVILEGED" | "USER" | "SUPERUSER";
  name: string;
}
