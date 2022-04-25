import { HttpClient, HttpHeaders, HttpRequest, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map as __map, filter as __filter } from 'rxjs/operators';

import { ApiConfiguration } from '../../api-configuration';
import { BaseService } from '../../base-service';
import { CreateNeoTokenRequest } from '../../models/blockchain/create-neo-token-request';
import { CreateNeoTokenSpecRequest } from '../../models/blockchain/create-neo-token-spec-request';
import { PaginationNeoToken } from '../../models/blockchain/pagination-neo-token';
import { TokenSpecTab } from '../../models/blockchain/token-spec-tab';
import { UpdateNeoTokenRequest } from '../../models/blockchain/update-neo-token-request';
import { StrictHttpResponse } from '../../strict-http-response';

@Injectable({
  providedIn: 'root'
})
class NeoTokenSpecsService extends BaseService {

  constructor(
    config: ApiConfiguration,
    http: HttpClient
  ) {
    super(config, http);
  }

  /**
   * Gets a pagination of Neo Tokens for the given query.
   * @param params The `NeoTokensService.GetNeoTokensParams` containing the following parameters:
   *
   * - `offset`:
   *
   * - `count`:
   *
   * @return successful operation
   */
   getTokenTemplatesResponse(params: NeoTokenSpecsService.GetNeoTokensParams): Observable<StrictHttpResponse<PaginationNeoToken>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;
    (params.tags || []).forEach((val, index) => {if (val != null) __params = __params.append('tags', val.toString())});
    if (params.offset != null) __params = __params.set('offset', params.offset.toString());
    if (params.count != null) __params = __params.set('count', params.count.toString());
    let req = new HttpRequest<any>(
      'GET',
      this.rootUrl + `/blockchain/token/template`,
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
   * @return successful operation
   */
   getTokenTemplates(params: NeoTokenSpecsService.GetNeoTokensParams): Observable<PaginationNeoToken> {
    return this.getTokenTemplatesResponse(params).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Creates a new NeoToken definition.
   * @param body undefined
   * @return successful operation
   */
   createTokenSpecResponse(body?: CreateNeoTokenSpecRequest): Observable<StrictHttpResponse<TokenSpecTab>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;
    __body = body;
    let req = new HttpRequest<any>(
      'POST',
      this.rootUrl + `/blockchain/token/template`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<TokenSpecTab>;
      })
    );
  }

  /**
   * Creates a new NeoToken definition.
   * @param body undefined
   * @return successful operation
   */
  createTokenSpec(body?: CreateNeoTokenSpecRequest): Observable<TokenSpecTab> {
    return this.createTokenSpecResponse(body).pipe(
      __map(_r => _r.body)
    );
  }
}

module NeoTokenSpecsService {

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
    query?: string;
  }
}

export { NeoTokenSpecsService }
