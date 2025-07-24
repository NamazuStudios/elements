import {MetadataSpec} from "./metadata-spec-tab";
import {UserLevel} from "../../users/user-dialog/user-dialog.component";


export interface Metadata {
  id: string;
  name: string;
  level: UserLevel
  spec?: MetadataSpec;
  metadata?: {[key: string]: any};
}

export interface CreateMetadataRequest {
  name: string;
  level: UserLevel
  spec?: MetadataSpec;
  metadata?: {[key: string]: any};
}
