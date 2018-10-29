import {ApplicationConfiguration} from "../api/models/application-configuration";
import {Application} from "../api/models/application";

export class ApplicationConfigurationViewModel implements ApplicationConfiguration {
  category: "MATCHMAKING" | "PSN_PS4" | "PSN_VITA" | "IOS_APP_STORE" | "ANDROID_GOOGLE_PLAY" | "FACEBOOK" | "FIREBASE" | "AMAZON_GAME_ON";
  id: string;
  parent: Application;
  uniqueIdentifier: string;
}
