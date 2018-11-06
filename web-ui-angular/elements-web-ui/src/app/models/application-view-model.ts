import {Application} from "../api/models/application";

export class ApplicationViewModel implements Application {
  description: string;
  httpDocumentationUiUrl: string;
  httpDocumentationUrl: string;
  httpTunnelEndpointUrl: string;
  id: string;
  name: string;
  scriptRepoUrl: string;
}
