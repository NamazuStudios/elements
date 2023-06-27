import { NeoToken } from "src/app/api/models/blockchain/neo-token";
import { Token } from "src/app/api/models/blockchain/token";

export class NeoTokenViewModel implements NeoToken {
  /**
   * The unique ID of the token itself.
   */
  id: string;

  token?: Token;

  /**
   * The elements contract id to mint this token with.
   */
  contractId: string;

  listed: boolean;

  minted: boolean;
}
