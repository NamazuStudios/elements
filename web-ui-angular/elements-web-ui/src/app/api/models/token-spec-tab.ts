export enum TokenSpecTabFieldTypes {
  STRING = 'String',
  NUMBER = 'Number',
  BOOLEAN = 'Boolean',
  ENUM = 'Enum',
  OBJECT = 'Object',
  TAGS = 'Tags',
  ARRAY = 'Array',
}

export interface TokenSpecTabField {
  name: string;
  fieldType: TokenSpecTabFieldTypes;
  content: any;
  fieldContentType?: string;
  value?: any;
  placeHolder?: string;
  defaultValue?: string;
}

export interface TokenSpecTab {
  name: string;
  fields: TokenSpecTabField[];
}

export interface TokenTemplate {
  contractId: string;
  id: string;
  name: string;
  tabs: TokenSpecTab[];
}
