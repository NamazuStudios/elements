export interface UpdateNeoTokenSpecRequest {
  /**
   * Tabs listed.
   */
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
