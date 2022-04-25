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
  type: TokenSpecTabFieldTypes;
  content: string;
}

export interface TokenSpecTab {
  name: string;
  fields: TokenSpecTabField[];
}
