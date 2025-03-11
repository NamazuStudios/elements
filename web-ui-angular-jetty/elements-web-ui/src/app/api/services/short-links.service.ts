/* tslint:disable */
import { Injectable } from '@angular/core';
import { HttpClient, HttpRequest, HttpResponse, HttpHeaders, HttpParams } from '@angular/common/http';
import { BaseService } from '../base-service';
import { ApiConfiguration } from '../api-configuration';
import { StrictHttpResponse } from '../strict-http-response';
import { Observable } from 'rxjs';
import { map as __map, filter as __filter } from 'rxjs/operators';

import { PaginationShortLink } from '../models/pagination-short-link';
import { ShortLink } from '../models/short-link';
@Injectable({
  providedIn: 'root',
})
class ShortLinksService extends BaseService {
  constructor(
    config: ApiConfiguration,
    http: HttpClient
  ) {
    super(config, http);
  }

  /**
   * Process a ShortLink by generating the proper HTTP response by following the link.  This shouldn't be used as the actual destination for a ShortLink as it is likely to be longer than the original link.  However, this can be used as the destination for some upstream service as a target.  Examples include Servlet forwards or HTTP request rewrites.
   * @param path undefined
   */
  getRedirectionResponse(path: string): Observable<StrictHttpResponse<void>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    let req = new HttpRequest<any>(
      'GET',
      this.rootUrl + `/short_link/redirection/${path}`,
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
   * Process a ShortLink by generating the proper HTTP response by following the link.  This shouldn't be used as the actual destination for a ShortLink as it is likely to be longer than the original link.  However, this can be used as the destination for some upstream service as a target.  Examples include Servlet forwards or HTTP request rewrites.
   * @param path undefined
   */
  getRedirection(path: string): Observable<void> {
    return this.getRedirectionResponse(path).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Gets a listing of all ShortLinks with the given search filter.
   * @param params The `ShortLinksService.GetShortLinksParams` containing the following parameters:
   *
   * - `search`:
   *
   * - `offset`:
   *
   * - `count`:
   *
   * @return successful operation
   */
  getShortLinksResponse(params: ShortLinksService.GetShortLinksParams): Observable<StrictHttpResponse<PaginationShortLink>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;
    if (params.search != null) __params = __params.set('search', params.search.toString());
    if (params.offset != null) __params = __params.set('offset', params.offset.toString());
    if (params.count != null) __params = __params.set('count', params.count.toString());
    let req = new HttpRequest<any>(
      'GET',
      this.rootUrl + `/short_link`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<PaginationShortLink>;
      })
    );
  }

  /**
   * Gets a listing of all ShortLinks with the given search filter.
   * @param params The `ShortLinksService.GetShortLinksParams` containing the following parameters:
   *
   * - `search`:
   *
   * - `offset`:
   *
   * - `count`:
   *
   * @return successful operation
   */
  getShortLinks(params: ShortLinksService.GetShortLinksParams): Observable<PaginationShortLink> {
    return this.getShortLinksResponse(params).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Creates a new ShortLink in the system, and returns the information needed to refer to the link later.
   * @param body undefined
   * @return successful operation
   */
  createResponse(body?: ShortLink): Observable<StrictHttpResponse<ShortLink>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;
    __body = body;
    let req = new HttpRequest<any>(
      'POST',
      this.rootUrl + `/short_link`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<ShortLink>;
      })
    );
  }

  /**
   * Creates a new ShortLink in the system, and returns the information needed to refer to the link later.
   * @param body undefined
   * @return successful operation
   */
  create(body?: ShortLink): Observable<ShortLink> {
    return this.createResponse(body).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Gets the metadata for a specific short link with the given ID.
   * @param id undefined
   * @return successful operation
   */
  getShortLinkResponse(id: string): Observable<StrictHttpResponse<ShortLink>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    let req = new HttpRequest<any>(
      'GET',
      this.rootUrl + `/short_link/${id}`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<ShortLink>;
      })
    );
  }

  /**
   * Gets the metadata for a specific short link with the given ID.
   * @param id undefined
   * @return successful operation
   */
  getShortLink(id: string): Observable<ShortLink> {
    return this.getShortLinkResponse(id).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Deletes a short link known to the server.  LazyValue delete, a short link will no longer resolve to its address an will be gone from the server permanently.
   * @param id undefined
   */
  deleteResponse(id: string): Observable<StrictHttpResponse<void>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    let req = new HttpRequest<any>(
      'DELETE',
      this.rootUrl + `/short_link/${id}`,
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
   * Deletes a short link known to the server.  LazyValue delete, a short link will no longer resolve to its address an will be gone from the server permanently.
   * @param id undefined
   */
  delete(id: string): Observable<void> {
    return this.deleteResponse(id).pipe(
      __map(_r => _r.body)
    );
  }
}

module ShortLinksService {

  /**
   * Parameters for getShortLinks
   */
  export interface GetShortLinksParams {
    search?: string;
    offset?: number;
    count?: number;
  }
}

export { ShortLinksService }
