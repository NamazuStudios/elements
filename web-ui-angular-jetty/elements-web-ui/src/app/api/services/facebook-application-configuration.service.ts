/* tslint:disable */
import { Injectable } from '@angular/core';
import { HttpClient, HttpRequest, HttpResponse, HttpHeaders, HttpParams } from '@angular/common/http';
import { BaseService } from '../base-service';
import { ApiConfiguration } from '../api-configuration';
import { StrictHttpResponse } from '../strict-http-response';
import { Observable } from 'rxjs';
import { map as __map, filter as __filter } from 'rxjs/operators';

import { FacebookApplicationConfiguration } from '../models/facebook-application-configuration';
@Injectable({
  providedIn: 'root',
})
class FacebookApplicationConfigurationService extends BaseService {
  constructor(
    config: ApiConfiguration,
    http: HttpClient
  ) {
    super(config, http);
  }

  /**
   * Gets a single Facebook application based on unique name or ID.
   * @param params The `FacebookApplicationConfigurationService.GetFacebookApplicationConfigurationParams` containing the following parameters:
   *
   * - `applicationNameOrId`:
   *
   * - `applicationConfigurationNameOrId`:
   *
   * @return successful operation
   */
  getFacebookApplicationConfigurationResponse(params: FacebookApplicationConfigurationService.GetFacebookApplicationConfigurationParams): Observable<StrictHttpResponse<FacebookApplicationConfiguration>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;


    let req = new HttpRequest<any>(
      'GET',
      this.rootUrl + `/application/${params.applicationNameOrId}/configuration/facebook/${params.applicationConfigurationNameOrId}`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<FacebookApplicationConfiguration>;
      })
    );
  }

  /**
   * Gets a single Facebook application based on unique name or ID.
   * @param params The `FacebookApplicationConfigurationService.GetFacebookApplicationConfigurationParams` containing the following parameters:
   *
   * - `applicationNameOrId`:
   *
   * - `applicationConfigurationNameOrId`:
   *
   * @return successful operation
   */
  getFacebookApplicationConfiguration(params: FacebookApplicationConfigurationService.GetFacebookApplicationConfigurationParams): Observable<FacebookApplicationConfiguration> {
    return this.getFacebookApplicationConfigurationResponse(params).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Updates an existing Facebook Application profile if it is known to the server.
   * @param params The `FacebookApplicationConfigurationService.UpdateApplicationConfigurationParams` containing the following parameters:
   *
   * - `applicationNameOrId`:
   *
   * - `applicationConfigurationNameOrId`:
   *
   * - `body`:
   *
   * @return successful operation
   */
  updateApplicationConfigurationResponse(params: FacebookApplicationConfigurationService.UpdateApplicationConfigurationParams): Observable<StrictHttpResponse<FacebookApplicationConfiguration>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;


    __body = params.body;
    let req = new HttpRequest<any>(
      'PUT',
      this.rootUrl + `/application/${params.applicationNameOrId}/configuration/facebook/${params.applicationConfigurationNameOrId}`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<FacebookApplicationConfiguration>;
      })
    );
  }

  /**
   * Updates an existing Facebook Application profile if it is known to the server.
   * @param params The `FacebookApplicationConfigurationService.UpdateApplicationConfigurationParams` containing the following parameters:
   *
   * - `applicationNameOrId`:
   *
   * - `applicationConfigurationNameOrId`:
   *
   * - `body`:
   *
   * @return successful operation
   */
  updateApplicationConfiguration(params: FacebookApplicationConfigurationService.UpdateApplicationConfigurationParams): Observable<FacebookApplicationConfiguration> {
    return this.updateApplicationConfigurationResponse(params).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Deletes an existing Facebook Application profile if it is known to the server.
   * @param params The `FacebookApplicationConfigurationService.DeleteFacebookApplicationConfigurationParams` containing the following parameters:
   *
   * - `applicationNameOrId`:
   *
   * - `applicationConfigurationNameOrId`:
   */
  deleteFacebookApplicationConfigurationResponse(params: FacebookApplicationConfigurationService.DeleteFacebookApplicationConfigurationParams): Observable<StrictHttpResponse<void>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;


    let req = new HttpRequest<any>(
      'DELETE',
      this.rootUrl + `/application/${params.applicationNameOrId}/configuration/facebook/${params.applicationConfigurationNameOrId}`,
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
   * Deletes an existing Facebook Application profile if it is known to the server.
   * @param params The `FacebookApplicationConfigurationService.DeleteFacebookApplicationConfigurationParams` containing the following parameters:
   *
   * - `applicationNameOrId`:
   *
   * - `applicationConfigurationNameOrId`:
   */
  deleteFacebookApplicationConfiguration(params: FacebookApplicationConfigurationService.DeleteFacebookApplicationConfigurationParams): Observable<void> {
    return this.deleteFacebookApplicationConfigurationResponse(params).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Creates a new Facebook ApplicationConfiguration with the specific ID or application.
   * @param params The `FacebookApplicationConfigurationService.CreateFacebookApplicationConfigurationParams` containing the following parameters:
   *
   * - `applicationNameOrId`:
   *
   * - `body`:
   *
   * @return successful operation
   */
  createFacebookApplicationConfigurationResponse(params: FacebookApplicationConfigurationService.CreateFacebookApplicationConfigurationParams): Observable<StrictHttpResponse<FacebookApplicationConfiguration>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    __body = params.body;
    let req = new HttpRequest<any>(
      'POST',
      this.rootUrl + `/application/${params.applicationNameOrId}/configuration/facebook`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<FacebookApplicationConfiguration>;
      })
    );
  }

  /**
   * Creates a new Facebook ApplicationConfiguration with the specific ID or application.
   * @param params The `FacebookApplicationConfigurationService.CreateFacebookApplicationConfigurationParams` containing the following parameters:
   *
   * - `applicationNameOrId`:
   *
   * - `body`:
   *
   * @return successful operation
   */
  createFacebookApplicationConfiguration(params: FacebookApplicationConfigurationService.CreateFacebookApplicationConfigurationParams): Observable<FacebookApplicationConfiguration> {
    return this.createFacebookApplicationConfigurationResponse(params).pipe(
      __map(_r => _r.body)
    );
  }
}

module FacebookApplicationConfigurationService {

  /**
   * Parameters for getFacebookApplicationConfiguration
   */
  export interface GetFacebookApplicationConfigurationParams {
    applicationNameOrId: string;
    applicationConfigurationNameOrId: string;
  }

  /**
   * Parameters for updateApplicationConfiguration
   */
  export interface UpdateApplicationConfigurationParams {
    applicationNameOrId: string;
    applicationConfigurationNameOrId: string;
    body?: FacebookApplicationConfiguration;
  }

  /**
   * Parameters for deleteFacebookApplicationConfiguration
   */
  export interface DeleteFacebookApplicationConfigurationParams {
    applicationNameOrId: string;
    applicationConfigurationNameOrId: string;
  }

  /**
   * Parameters for createFacebookApplicationConfiguration
   */
  export interface CreateFacebookApplicationConfigurationParams {
    applicationNameOrId: string;
    body?: FacebookApplicationConfiguration;
  }
}

export { FacebookApplicationConfigurationService }
