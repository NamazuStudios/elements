import {MetadataSpecProperty, MetadataSpecPropertyType} from "./token-spec-tab";

export interface CreateNeoTokenSpecRequest {
  /**
   * Properties listed.
   */
  // tabs: TokenSpecTab[];
  properties: MetadataSpecProperty[];

  /**
   * The token spec name.
   */
  name: string;

  /**
   * The type of property.
   */
  type: MetadataSpecPropertyType;
}
