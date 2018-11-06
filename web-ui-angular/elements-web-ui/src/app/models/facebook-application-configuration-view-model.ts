import {FacebookApplicationConfiguration} from "../api/models/facebook-application-configuration";
import {Application} from "../api/models/application";

export class FacebookApplicationConfigurationViewModel implements FacebookApplicationConfiguration {
  applicationId: string;
  applicationSecret: string;
  builtinApplicationPermissions: Array<string>;
  category: "FACEBOOK" ;
  id: string;
  parent: Application;
  uniqueIdentifier: string;
}
