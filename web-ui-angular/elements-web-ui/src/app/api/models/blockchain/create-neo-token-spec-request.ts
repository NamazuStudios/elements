import { TokenSpecTab } from "./token-spec-tab";

export interface CreateNeoTokenSpecRequest {
  /**
   * Tabs listed.
   */
  tabs: TokenSpecTab[];

  /**
   * The token spec name.
   */
  tokenName: string;

  /**
   * The contract id.
   */
   contractId: string;
}
