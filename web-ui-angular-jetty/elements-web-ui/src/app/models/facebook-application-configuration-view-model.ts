import {FacebookApplicationConfiguration} from "../api/models/facebook-application-configuration";
import {Application} from "../api/models/application";
import {ApplicationConfigurationViewModel} from "./application-configuration-view-model";

export class FacebookApplicationConfigurationViewModel extends ApplicationConfigurationViewModel implements FacebookApplicationConfiguration {

  applicationId: string;

  applicationSecret: string;

  builtinApplicationPermissions: Array<string>;

}
