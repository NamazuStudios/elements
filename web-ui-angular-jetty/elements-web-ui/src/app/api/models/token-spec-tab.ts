export enum MetadataSpecPropertyType {
  STRING = 'STRING',
  NUMBER = 'NUMBER',
  BOOLEAN = 'BOOLEAN',
  OBJECT = 'OBJECT',
  TAGS = 'TAGS',
  ARRAY = 'ARRAY',
}

export interface MetadataSpecProperty {
  name: string;
  displayName: string;
  type: MetadataSpecPropertyType;
  required: boolean;
  placeholder?: string;
  defaultValue?: string;
  properties?: MetadataSpecProperty[];
}

export interface MetadataSpec {
  id: string;
  name: string;
  type: MetadataSpecPropertyType;
  properties?: MetadataSpecProperty[];
}

export interface CreateMetadataSpecRequest {
  name: string;
  type: MetadataSpecPropertyType;
  properties?: MetadataSpecProperty[];
}
