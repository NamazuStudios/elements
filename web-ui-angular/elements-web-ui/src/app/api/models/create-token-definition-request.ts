export interface CreateTokenDefinitionRequest {
  name: string;
  displayName: string;
  metadataSpecId: string;
  contractId: string;
  metadata: any;
  userId?: string;
}
