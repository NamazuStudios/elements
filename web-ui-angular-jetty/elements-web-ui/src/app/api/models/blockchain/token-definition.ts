import { MetadataSpec } from '../token-spec-tab';
import { NeoSmartContract } from './neo-smart-contract';

export interface TokenDefinition {
  contract: NeoSmartContract;
  displayName: string;
  id: string;
  metadata: any;
  metadataSpec: MetadataSpec;
  name: string;
  user?: string;
}
