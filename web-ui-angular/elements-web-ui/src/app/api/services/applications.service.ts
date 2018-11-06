/* tslint:disable */
import { Injectable } from '@angular/core';
import { HttpClient, HttpRequest, HttpResponse, HttpHeaders, HttpParams } from '@angular/common/http';
import { BaseService } from '../base-service';
import { ApiConfiguration } from '../api-configuration';
import { StrictHttpResponse } from '../strict-http-response';
import { Observable } from 'rxjs';
import { map as __map, filter as __filter } from 'rxjs/operators';

import { Application } from '../models/application';
import { PaginationApplication } from '../models/pagination-application';
@Injectable({
  providedIn: 'root',
})
class ApplicationsService extends BaseService {
  constructor(
    config: ApiConfiguration,
    http: HttpClient
  ) {
    super(config, http);
  }

  /**
   * Gets the metadata for a single application.  This may include more specific details not availble in the bulk-get or fetch operation.
   * @param nameOrId undefined
   * @return successful operation
   */
  getApplicationResponse(nameOrId: string): Observable<StrictHttpResponse<Application>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    let req = new HttpRequest<any>(
      'GET',
      this.rootUrl + `/application/${nameOrId}`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<Application>;
      })
    );
  }

  /**
   * Gets the metadata for a single application.  This may include more specific details not availble in the bulk-get or fetch operation.
   * @param nameOrId undefined
   * @return successful operation
   */
  getApplication(nameOrId: string): Observable<Application> {
    return this.getApplicationResponse(nameOrId).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Performs an update to an existing application known to the server.
   * @param params The `ApplicationsService.UpdateApplicationParams` containing the following parameters:
   *
   * - `nameOrId`:
   *
   * - `body`:
   *
   * @return successful operation
   */
  updateApplicationResponse(params: ApplicationsService.UpdateApplicationParams): Observable<StrictHttpResponse<Application>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    __body = params.body;
    let req = new HttpRequest<any>(
      'PUT',
      this.rootUrl + `/application/${params.nameOrId}`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<Application>;
      })
    );
  }

  /**
   * Performs an update to an existing application known to the server.
   * @param params The `ApplicationsService.UpdateApplicationParams` containing the following parameters:
   *
   * - `nameOrId`:
   *
   * - `body`:
   *
   * @return successful operation
   */
  updateApplication(params: ApplicationsService.UpdateApplicationParams): Observable<Application> {
    return this.updateApplicationResponse(params).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Deletes a specific application known to the server.
   * @param nameOrId undefined
   */
  deleteApplicationResponse(nameOrId: string): Observable<StrictHttpResponse<void>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    let req = new HttpRequest<any>(
      'DELETE',
      this.rootUrl + `/application/${nameOrId}`,
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
   * Deletes a specific application known to the server.
   * @param nameOrId undefined
   */
  deleteApplication(nameOrId: string): Observable<void> {
    return this.deleteApplicationResponse(nameOrId).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Performs a full-text search of all applications known to the server.  As with other full-text endpoints this allows for pagination and offset.
   * @param params The `ApplicationsService.GetApplicationsParams` containing the following parameters:
   *
   * - `search`:
   *
   * - `offset`:
   *
   * - `count`:
   *
   * @return successful operation
   */
  getApplicationsResponse(params: ApplicationsService.GetApplicationsParams): Observable<StrictHttpResponse<PaginationApplication>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;
    if (params.search != null) __params = __params.set('search', params.search.toString());
    if (params.offset != null) __params = __params.set('offset', params.offset.toString());
    if (params.count != null) __params = __params.set('count', params.count.toString());
    let req = new HttpRequest<any>(
      'GET',
      this.rootUrl + `/application`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<PaginationApplication>;
      })
    );
  }

  /**
   * Performs a full-text search of all applications known to the server.  As with other full-text endpoints this allows for pagination and offset.
   * @param params The `ApplicationsService.GetApplicationsParams` containing the following parameters:
   *
   * - `search`:
   *
   * - `offset`:
   *
   * - `count`:
   *
   * @return successful operation
   */
  getApplications(params: ApplicationsService.GetApplicationsParams): Observable<PaginationApplication> {
    return this.getApplicationsResponse(params).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Gets the metadata for a single application.  This may include more specific details not available in the bulk-get or fetch operation.
   * @param body undefined
   * @return successful operation
   */
  createApplicationResponse(body?: Application): Observable<StrictHttpResponse<Application>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;
    __body = body;
    let req = new HttpRequest<any>(
      'POST',
      this.rootUrl + `/application`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<Application>;
      })
    );
  }

  /**
   * Gets the metadata for a single application.  This may include more specific details not available in the bulk-get or fetch operation.
   * @param body undefined
   * @return successful operation
   */
  createApplication(body?: Application): Observable<Application> {
    return this.createApplicationResponse(body).pipe(
      __map(_r => _r.body)
    );
  }
}

module ApplicationsService {

  /**
   * Parameters for updateApplication
   */
  export interface UpdateApplicationParams {
    nameOrId: string;
    body?: Application;
  }

  /**
   * Parameters for getApplications
   */
  export interface GetApplicationsParams {
    search?: string;
    offset?: number;
    count?: number;
  }
}

export { ApplicationsService }
