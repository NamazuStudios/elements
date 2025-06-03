import {ApplicationConfiguration} from "../api/models/application-configuration";
import {Application} from "../api/models/application";

export class ApplicationConfigurationViewModel implements ApplicationConfiguration {

  description: string;

  id: string;

  name: string;

  parent: Application;

  type: string;

}
