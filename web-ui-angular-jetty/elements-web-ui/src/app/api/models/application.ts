/* tslint:disable */
export interface Application {
  id?: string;
  name: string;
  description?: string;
  scriptRepoUrl?: string;
  httpDocumentationUrl?: string;
  httpDocumentationUiUrl?: string;
  httpTunnelEndpointUrl?: string;
  attributes?: {[key: string]: any};
}
