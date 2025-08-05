import {MetadataSpec} from "./metadata-spec-tab";
import {UserLevel} from "../../users/user-dialog/user-dialog.component";


export interface Metadata {
  id: string;
  name: string;
  accessLevel: UserLevel
  metadataSpec?: MetadataSpec;
  metadata?: {[key: string]: any};
}

export interface CreateMetadataRequest {
  name: string;
  accessLevel: UserLevel
  metadataSpec?: MetadataSpec;
  metadata?: {[key: string]: any};
}

export interface UpdateMetadataRequest {
  accessLevel: UserLevel
  metadataSpec?: MetadataSpec;
  metadata?: {[key: string]: any};
}
