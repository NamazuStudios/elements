import {HttpClient, HttpHeaders, HttpRequest, HttpResponse} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {filter as __filter, map as __map} from 'rxjs/operators';

import {ApiConfiguration} from '../api-configuration';
import {BaseService} from '../base-service';
import {PaginationMetadata} from '../models/pagination-metadata';
import {StrictHttpResponse} from '../strict-http-response';
import {CreateMetadataRequest, Metadata, UpdateMetadataRequest} from "../models/metadata-tab";

@Injectable({
  providedIn: 'root'
})
class MetadataService extends BaseService {

  constructor(
    config: ApiConfiguration,
    http: HttpClient
  ) {
    super(config, http);
  }

  /**
   * Gets a pagination of Metadata for the given query.
   * @param params The `MetadataService.GetMetadataParams` containing the following parameters:
   *
   * - `offset`:
   *
   * - `count`:
   *
   * @return successful operation
   */
  getMetadatasResponse(params: MetadataService.GetMetadataParams): Observable<StrictHttpResponse<PaginationMetadata>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;
    if (params.offset != null) __params = __params.set('offset', params.offset.toString());
    if (params.count != null) __params = __params.set('count', params.count.toString());
    let req = new HttpRequest<any>(
      'GET',
      this.rootUrl + `/metadata`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<PaginationMetadata>;
      })
    );
  }

  /**
   * Gets a pagination of metadata specs for the given query.
   * @param params The `MetadataService.GetMetadataParams` containing the following parameters:
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
  getMetadatas(params: MetadataService.GetMetadataParams): Observable<PaginationMetadata> {
    return this.getMetadatasResponse(params).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Creates a new Metadata definition.
   * @param body undefined
   * @return successful operation
   */
  createMetadataResponse(body?: CreateMetadataRequest): Observable<StrictHttpResponse<Metadata>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = body;

    let req = new HttpRequest<any>(
      'POST',
      this.rootUrl + `/metadata`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<Metadata>;
      })
    );
  }

  /**
   * Creates a new Metadata definition.
   * @param body undefined
   * @return successful operation
   */
  createMetadata(body?: CreateMetadataRequest): Observable<Metadata> {
    return this.createMetadataResponse(body).pipe(
      __map(_r => _r.body)
    );
  }


  /**
   * Gets a specific Metadata by name or Id.
   * @param tokenNameOrId undefined
   * @return successful operation
   */
  getMetadataResponse(tokenNameOrId: string): Observable<StrictHttpResponse<Metadata>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    let req = new HttpRequest<any>(
      'GET',
      this.rootUrl + `/metadata/${tokenNameOrId}`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<Metadata>;
      })
    );
  }

  /**
   * Gets a specific Metadata by token name or Id.
   * @param tokenNameOrId undefined
   * @return successful operation
   */
  getMetadata(tokenNameOrId: string): Observable<Metadata> {
    return this.getMetadataResponse(tokenNameOrId).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Updates a Neo Token with the specified tokenId.
   * @param params The `MetadataService.UpdateTokenParams` containing the following parameters:
   *
   * - `id`:
   *
   * - `body`:
   *
   * @return successful operation
   */
  updateMetadataResponse(specId: string, request: UpdateMetadataRequest): Observable<StrictHttpResponse<Metadata>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    __body = request;
    let req = new HttpRequest<any>(
      'PUT',
      this.rootUrl + `/metadata/${specId}`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<Metadata>;
      })
    );
  }

  /**
   * Updates a Neo Token with the specified tokenId.
   * @param params The `MetadataService.UpdateTokenParams` containing the following parameters:
   *
   * - `tokenId`:
   *
   * - `body`:
   *
   * @return successful operation
   */
  updateMetadata(specId: string, request: UpdateMetadataRequest): Observable<Metadata> {
    return this.updateMetadataResponse(specId, request).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Deletes a Neo Token with the specified id.
   * @param tokenId undefined
   */
  deleteMetadataResponse(tokenId: string): Observable<StrictHttpResponse<void>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    let req = new HttpRequest<any>(
      'DELETE',
      this.rootUrl + `/metadata/${tokenId}`,
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
  deleteMetadata(tokenId: string): Observable<void> {
    return this.deleteMetadataResponse(tokenId).pipe(
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
        return _r as StrictHttpResponse<Metadata>;
      })
    );
  }
}

module MetadataService {

  /**
   * Parameters for getTokens
   */
  export interface GetMetadataParams {
    offset?: number;
    count?: number;
    tags?: Array<string>;
    query?: string;
  }
}

export { MetadataService }
