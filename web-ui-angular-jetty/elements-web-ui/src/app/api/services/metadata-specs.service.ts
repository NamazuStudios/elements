import {HttpClient, HttpHeaders, HttpRequest, HttpResponse} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {filter as __filter, map as __map} from 'rxjs/operators';

import {ApiConfiguration} from '../api-configuration';
import {BaseService} from '../base-service';
import {PaginationNeoTokenSpec} from '../models/pagination-neo-token-spec';
import {StrictHttpResponse} from '../strict-http-response';
import {CreateMetadataSpecRequest, MetadataSpec} from "../models/token-spec-tab";

@Injectable({
  providedIn: 'root'
})
class MetadataSpecsService extends BaseService {

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
   getTokenTemplatesResponse(params: MetadataSpecsService.GetNeoTokensParams): Observable<StrictHttpResponse<PaginationNeoTokenSpec>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;
    if (params.offset != null) __params = __params.set('offset', params.offset.toString());
    if (params.count != null) __params = __params.set('count', params.count.toString());
    let req = new HttpRequest<any>(
      'GET',
      this.rootUrl + `/metadata_spec`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<PaginationNeoTokenSpec>;
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
   getTokenTemplates(params: MetadataSpecsService.GetNeoTokensParams): Observable<PaginationNeoTokenSpec> {
    return this.getTokenTemplatesResponse(params).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Creates a new NeoToken definition.
   * @param body undefined
   * @return successful operation
   */
   createTokenSpecResponse(body?: CreateMetadataSpecRequest): Observable<StrictHttpResponse<MetadataSpec>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = body;

    let req = new HttpRequest<any>(
      'POST',
      this.rootUrl + `/metadata_spec`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<MetadataSpec>;
      })
    );
  }

  /**
   * Creates a new NeoToken definition.
   * @param body undefined
   * @return successful operation
   */
  createTokenSpec(body?: CreateMetadataSpecRequest): Observable<MetadataSpec> {
    return this.createTokenSpecResponse(body).pipe(
      __map(_r => _r.body)
    );
  }


  /**
   * Gets a specific NeoToken by token name or Id.
   * @param tokenNameOrId undefined
   * @return successful operation
   */
   getTokenTemplateResponse(tokenNameOrId: string): Observable<StrictHttpResponse<MetadataSpec>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    let req = new HttpRequest<any>(
      'GET',
      this.rootUrl + `/metadata_spec/${tokenNameOrId}`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<MetadataSpec>;
      })
    );
  }

  /**
   * Gets a specific NeoToken by token name or Id.
   * @param tokenNameOrId undefined
   * @return successful operation
   */
  getTokenTemplate(tokenNameOrId: string): Observable<MetadataSpec> {
    return this.getTokenTemplateResponse(tokenNameOrId).pipe(
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
   updateTokenTemplateResponse(specId: string, request: CreateMetadataSpecRequest): Observable<StrictHttpResponse<MetadataSpec>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    __body = request;
    let req = new HttpRequest<any>(
      'PUT',
      this.rootUrl + `/metadata_spec/${specId}`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<MetadataSpec>;
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
  updateTokenTemplate(specId: string, request: CreateMetadataSpecRequest): Observable<MetadataSpec> {
    return this.updateTokenTemplateResponse(specId, request).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Deletes a Neo Token with the specified id.
   * @param tokenId undefined
   */
   deleteTokenTemplateResponse(tokenId: string): Observable<StrictHttpResponse<void>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    let req = new HttpRequest<any>(
      'DELETE',
      this.rootUrl + `/metadata_spec/${tokenId}`,
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
  deleteTokenTemplate(tokenId: string): Observable<void> {
    return this.deleteTokenTemplateResponse(tokenId).pipe(
      __map(_r => _r.body)
    );
  }

  callReindex() {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    __body = {
      plan: true,
      toIndex: ["DISTINCT_INVENTORY_ITEM"]
    };

    let req = new HttpRequest<any>(
      'POST',
      this.rootUrl + `/index/build`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<MetadataSpec>;
      })
    );
  }
}

module MetadataSpecsService {

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

export { MetadataSpecsService }
