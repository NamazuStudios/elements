/* tslint:disable */
export interface ContractParameter {

  /**
   * ??
   */
   readonly name: string;

  /**
   * ??
   */
   readonly type: 'ANY | BOOLEAN | INTEGER | BYTE_ARRAY | STRING | HASH160 | HASH256 | PUBLIC_KEY | SIGNATURE | ARRAY | MAP | INTEROP_INTERFACE | VOID';

  /**
   * ???
   */
   value: Object;
  
}
