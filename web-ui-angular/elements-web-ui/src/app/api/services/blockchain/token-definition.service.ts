/* tslint:disable */
import { Injectable } from '@angular/core';
import { HttpClient, HttpRequest, HttpResponse, HttpHeaders} from '@angular/common/http';
import { BaseService } from '../../base-service';
import { ApiConfiguration } from '../../api-configuration';
import { StrictHttpResponse } from '../../strict-http-response';
import { Observable } from 'rxjs';
import { map as __map, filter as __filter } from 'rxjs/operators';

import { CreateTokenDefinitionRequest } from '../../models/create-token-definition-request';
import { CreateTokenDefinitionResponse } from '../../models/create-token-definition-response';
import { PaginationTokenDefinition } from '../../models/blockchain/pagination-token-defination';
import { TokenDefinition } from '../../models/blockchain/token-definition';

@Injectable({
  providedIn: 'root',
})
class TokenDefinitionService extends BaseService {
  constructor(
    config: ApiConfiguration,
    http: HttpClient
  ) {
    super(config, http);
  }

  readonly network = 'NEO';

  /**
   * Gets a pagination of Neo Tokens for the given query.
   * @param params The `NeoTokensService.GetNeoTokensParams` containing the following parameters:
   *
   * - `tags`:
   *
   * - `query`:
   *
   * - `offset`:
   *
   * - `count`:
   *
   * @return successful operation
   */
   getTokenDefinitionsResponse(params: TokenDefinitionService.GetTokenDefinitionsParams): Observable<StrictHttpResponse<PaginationTokenDefinition>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;
    if (params.offset != null) __params = __params.set('offset', params.offset.toString());
    if (params.count != null) __params = __params.set('count', params.count.toString());
    let req = new HttpRequest<any>(
      'GET',
      this.rootUrl + `/schema/token_template`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<PaginationTokenDefinition>;
      })
    );
  }

  /**
   * Gets a pagination of Neo Tokens for the given query.
   * @param params The `NeoTokensService.GetNeoTokensParams` containing the following parameters:
   *
   * - `tags`:
   *
   * - `query`:
   *
   * - `offset`:
   *
   * - `count`:
   *
   * @return successful operation
   */
  getTokenDefinitions(params: TokenDefinitionService.GetTokenDefinitionsParams): Observable<PaginationTokenDefinition> {
    return this.getTokenDefinitionsResponse(params).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Creates a new NeoToken definition.
   * @param body undefined
   * @return successful operation
   */
   createTokenDefinitionResponse(body?: CreateTokenDefinitionRequest): Observable<StrictHttpResponse<CreateTokenDefinitionResponse>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;
    __body = body;
    let req = new HttpRequest<any>(
      'POST',
      this.rootUrl + `/schema/token_template`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<CreateTokenDefinitionResponse>;
      })
    );
  }

  /**
   * Creates a new NeoToken definition.
   * @param body undefined
   * @return successful operation
   */
   createTokenDefinition(body?: CreateTokenDefinitionRequest): Observable<CreateTokenDefinitionResponse> {
    return this.createTokenDefinitionResponse(body).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Updates a Neo Token with the specified tokenId.
   * @param params The `NeoTokensService.UpdateTokenParams` containing the following parameters:
   *
   * - `id`:
   *
   * - `body`:
   *
   * @return successful operation
   */
   updateTokenDefinitionResponse(params: TokenDefinitionService.UpdateTokenDefinitionParams): Observable<StrictHttpResponse<TokenDefinition>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    __body = params.body;
    let req = new HttpRequest<any>(
      'PUT',
      this.rootUrl + `/schema/token_template/${params.id}`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<TokenDefinition>;
      })
    );
  }

  /**
   * Updates a Neo Token with the specified tokenId.
   * @param params The `NeoTokenssService.UpdateTokenParams` containing the following parameters:
   *
   * - `tokenId`:
   *
   * - `body`:
   *
   * @return successful operation
   */
  updateTokenDefinition(params: TokenDefinitionService.UpdateTokenDefinitionParams): Observable<TokenDefinition> {
    return this.updateTokenDefinitionResponse(params).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Deletes a Neo Token with the specified id.
   * @param tokenId undefined
   */
   deleteTokenDefinitionResponse(tokenId: string): Observable<StrictHttpResponse<void>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    let req = new HttpRequest<any>(
      'DELETE',
      this.rootUrl + `/schema/token_template/${tokenId}`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'text'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r.clone({ body: null }) as StrictHttpResponse<void>
      })
    );
  }

  /**
   * Deletes a Neo Token with the specified id.
   * @param tokenId undefined
   */
  deleteTokenDefinition(tokenId: string): Observable<void> {
    return this.deleteTokenDefinitionResponse(tokenId).pipe(
      __map(_r => _r.body)
    );
  }
}

module TokenDefinitionService {

  /**
   * Parameters for updateToken
   */
  export interface UpdateTokenDefinitionParams {
    id: string;
    body?: CreateTokenDefinitionRequest;
  }

  /**
   * Parameters for get token definitions
   */
  export interface GetTokenDefinitionsParams {
    offset?: number;
    count?: number;
  }
}

export { TokenDefinitionService }
