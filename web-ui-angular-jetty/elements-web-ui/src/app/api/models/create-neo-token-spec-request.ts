export interface CreateNeoTokenSpecRequest {
  /**
   * Tabs listed.
   */
  // tabs: TokenSpecTab[];
  tabs: any[];

  /**
   * The token spec name.
   */
  name: string;

  /**
   * The contract id.
   */
  contractId?: string;
}
