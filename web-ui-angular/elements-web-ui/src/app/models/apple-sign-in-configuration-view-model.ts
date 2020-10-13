import {User} from "../api/models/user";
import {AppleSignInConfiguration} from "../api/models/apple-sign-in-configuration";

export class AppleSignInConfigurationViewModel implements AppleSignInConfiguration {
  keyId: string;
  teamId: string;
  clientId: string;
  appleSignInPrivateKey: string;
}
