/* tslint:disable */
import { Injectable } from '@angular/core';
import { HttpClient, HttpRequest, HttpResponse, HttpHeaders} from '@angular/common/http';
import { BaseService } from '../../base-service';
import { ApiConfiguration } from '../../api-configuration';
import { StrictHttpResponse } from '../../strict-http-response';
import { BehaviorSubject, Observable } from 'rxjs';
import { map as __map, filter as __filter } from 'rxjs/operators';
import { UpdateNeoTokenRequest } from '../../models/blockchain/update-neo-token-request';
import { NeoToken } from '../../models/blockchain/neo-token';
import { PaginationNeoToken } from '../../models/blockchain/pagination-neo-token';
import { CreateNeoTokenRequest } from '../../models/blockchain/create-neo-token-request';

@Injectable({
  providedIn: 'root',
})
class NeoTokensService extends BaseService {
  constructor(
    config: ApiConfiguration,
    http: HttpClient
  ) {
    super(config, http);
  }

  network = new BehaviorSubject('NEO');

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
   getTokensResponse(params: NeoTokensService.GetNeoTokensParams): Observable<StrictHttpResponse<PaginationNeoToken>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;
    (params.tags || []).forEach((val, index) => {if (val != null) __params = __params.append('tags', val.toString())});
    if (params.query != null) __params = __params.set('search', params.query.toString());
    if (params.offset != null) __params = __params.set('offset', params.offset.toString());
    if (params.count != null) __params = __params.set('count', params.count.toString());
    (params.mintStatus || []).forEach((val, index) => {if (val != null) __params = __params.append('mintStatus', val.toString())});
    
    let req = new HttpRequest<any>(
      'GET',
      `${this.rootUrl}/blockchain/${this.network.getValue().toLowerCase()}/token`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<PaginationNeoToken>;
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
   * - `mintStatus`:
   *
   * @return successful operation
   */
  getTokens(params: NeoTokensService.GetNeoTokensParams): Observable<PaginationNeoToken> {
    return this.getTokensResponse(params).pipe(
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
  updateTokenResponse(params: NeoTokensService.UpdateNeoTokenParams): Observable<StrictHttpResponse<NeoToken>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    __body = params.body;
    let req = new HttpRequest<any>(
      'PUT',
      `${this.rootUrl}/blockchain/${this.network.getValue().toLowerCase()}/token/${params.id}`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<NeoToken>;
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
  updateToken(params: NeoTokensService.UpdateNeoTokenParams): Observable<NeoToken> {
    return this.updateTokenResponse(params).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Deletes a Neo Token with the specified id.
   * @param tokenId undefined
   */
  deleteTokenResponse(tokenId: string): Observable<StrictHttpResponse<void>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    let req = new HttpRequest<any>(
      'DELETE',
      `${this.rootUrl}/blockchain/${this.network.getValue().toLowerCase()}/token/${tokenId}`,
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
  deleteToken(tokenId: string): Observable<void> {
    return this.deleteTokenResponse(tokenId).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Gets a specific NeoToken by token name or Id.
   * @param tokenNameOrId undefined
   * @return successful operation
   */
   getTokenResponse(tokenNameOrId: string): Observable<StrictHttpResponse<NeoToken>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    let req = new HttpRequest<any>(
      'GET',
      `${this.rootUrl}/blockchain/${this.network.getValue().toLowerCase()}/token/${tokenNameOrId}`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<NeoToken>;
      })
    );
  }

  /**
   * Gets a specific NeoToken by token name or Id.
   * @param tokenNameOrId undefined
   * @return successful operation
   */
  getToken(tokenNameOrId: string): Observable<NeoToken> {
    return this.getTokenResponse(tokenNameOrId).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Creates a new NeoToken definition.
   * @param body undefined
   * @return successful operation
   */
  createTokenResponse(body?: CreateNeoTokenRequest): Observable<StrictHttpResponse<NeoToken>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;
    __body = body;
    let req = new HttpRequest<any>(
      'POST',
      `${this.rootUrl}/blockchain/${this.network.getValue().toLowerCase()}/token`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<NeoToken>;
      })
    );
  }

  /**
   * Creates a new NeoToken definition.
   * @param body undefined
   * @return successful operation
   */
  createToken(body?: CreateNeoTokenRequest): Observable<NeoToken> {
    return this.createTokenResponse(body).pipe(
      __map(_r => _r.body)
    );
  }

 }

module NeoTokensService {

  /**
   * Parameters for updateToken
   */
  export interface UpdateNeoTokenParams {
    id: string;
    body?: UpdateNeoTokenRequest;
  }

  /**
   * Parameters for getTokens
   */
  export interface GetNeoTokensParams {
    offset?: number;
    count?: number;
    tags?: Array<string>;
    mintStatus?: Array<string>;
    query?: string;
  }
}

export { NeoTokensService }
