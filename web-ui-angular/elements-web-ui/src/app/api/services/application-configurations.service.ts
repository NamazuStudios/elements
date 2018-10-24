/* tslint:disable */
import { Injectable } from '@angular/core';
import { HttpClient, HttpRequest, HttpResponse, HttpHeaders, HttpParams } from '@angular/common/http';
import { BaseService } from '../base-service';
import { ApiConfiguration } from '../api-configuration';
import { StrictHttpResponse } from '../strict-http-response';
import { Observable } from 'rxjs';
import { map as __map, filter as __filter } from 'rxjs/operators';

import { PaginationApplicationConfiguration } from '../models/pagination-application-configuration';
@Injectable({
  providedIn: 'root',
})
class ApplicationConfigurationsService extends BaseService {
  constructor(
    config: ApiConfiguration,
    http: HttpClient
  ) {
    super(config, http);
  }

  /**
   * Searches all instances of ApplicationProfiles associated with the application.  The search query may be a full text search.
   * @param params The `ApplicationConfigurationsService.GetApplicationProfilesParams` containing the following parameters:
   *
   * - `applicationNameOrId`:
   *
   * - `search`:
   *
   * - `offset`:
   *
   * - `count`:
   *
   * @return successful operation
   */
  getApplicationProfilesResponse(params: ApplicationConfigurationsService.GetApplicationProfilesParams): Observable<StrictHttpResponse<PaginationApplicationConfiguration>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    if (params.search != null) __params = __params.set('search', params.search.toString());
    if (params.offset != null) __params = __params.set('offset', params.offset.toString());
    if (params.count != null) __params = __params.set('count', params.count.toString());
    let req = new HttpRequest<any>(
      'GET',
      this.rootUrl + `/application/${params.applicationNameOrId}/configuration`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<PaginationApplicationConfiguration>;
      })
    );
  }

  /**
   * Searches all instances of ApplicationProfiles associated with the application.  The search query may be a full text search.
   * @param params The `ApplicationConfigurationsService.GetApplicationProfilesParams` containing the following parameters:
   *
   * - `applicationNameOrId`:
   *
   * - `search`:
   *
   * - `offset`:
   *
   * - `count`:
   *
   * @return successful operation
   */
  getApplicationProfiles(params: ApplicationConfigurationsService.GetApplicationProfilesParams): Observable<PaginationApplicationConfiguration> {
    return this.getApplicationProfilesResponse(params).pipe(
      __map(_r => _r.body)
    );
  }
}

module ApplicationConfigurationsService {

  /**
   * Parameters for getApplicationProfiles
   */
  export interface GetApplicationProfilesParams {
    applicationNameOrId: string;
    search?: string;
    offset?: number;
    count?: number;
  }
}

export { ApplicationConfigurationsService }
