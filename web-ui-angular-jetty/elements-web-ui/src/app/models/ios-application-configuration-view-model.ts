
import {IosApplicationConfiguration} from "../api/models/ios-application-configuration";
import {ApplicationConfigurationViewModel} from "./application-configuration-view-model";
import {AppleSignInConfiguration} from "../api/models/apple-sign-in-configuration";

export class IosApplicationConfigurationViewModel extends ApplicationConfigurationViewModel implements IosApplicationConfiguration {

  appleSignInConfiguration?: AppleSignInConfiguration;

}
