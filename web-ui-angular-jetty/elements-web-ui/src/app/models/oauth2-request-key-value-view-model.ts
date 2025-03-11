import {Oauth2RequestKeyValue} from "../api/models/auth/oauth2-request-key-value";

export class Oauth2RequestKeyValueViewModel implements Oauth2RequestKeyValue {
  key: string;
  value: string;
  fromClient: boolean;

}
