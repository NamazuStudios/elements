import { TokenTemplate } from '../token-spec-tab';
import { NeoSmartContract } from './neo-smart-contract';

export interface TokenDefinition {
  contract: NeoSmartContract;
  displayName: string;
  id: string;
  metadata: any;
  metadataSpec: TokenTemplate;
  name: string;
  user?: string;
}
