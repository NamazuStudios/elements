/* tslint:disable */
import { Injectable } from '@angular/core';
import { HttpClient, HttpRequest, HttpResponse, HttpHeaders, HttpParams } from '@angular/common/http';
import { BaseService } from '../base-service';
import { ApiConfiguration } from '../api-configuration';
import { StrictHttpResponse } from '../strict-http-response';
import { Observable } from 'rxjs';
import { map as __map, filter as __filter } from 'rxjs/operators';

import { GooglePlayApplicationConfiguration } from '../models/google-play-application-configuration';
@Injectable({
  providedIn: 'root',
})
class GooglePlayApplicationConfigurationService extends BaseService {
  constructor(
    config: ApiConfiguration,
    http: HttpClient
  ) {
    super(config, http);
  }

  /**
   * Gets a single Google Play application based on unique name or ID.
   * @param params The `GooglePlayApplicationConfigurationService.GetGooglePlayApplicationConfigurationParams` containing the following parameters:
   *
   * - `applicationNameOrId`:
   *
   * - `applicationConfigurationNameOrId`:
   *
   * @return successful operation
   */
  getGooglePlayApplicationConfigurationResponse(params: GooglePlayApplicationConfigurationService.GetGooglePlayApplicationConfigurationParams): Observable<StrictHttpResponse<GooglePlayApplicationConfiguration>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;


    let req = new HttpRequest<any>(
      'GET',
      this.rootUrl + `/application/${params.applicationNameOrId}/configuration/google_play/${params.applicationConfigurationNameOrId}`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<GooglePlayApplicationConfiguration>;
      })
    );
  }

  /**
   * Gets a single Google Play application based on unique name or ID.
   * @param params The `GooglePlayApplicationConfigurationService.GetGooglePlayApplicationConfigurationParams` containing the following parameters:
   *
   * - `applicationNameOrId`:
   *
   * - `applicationConfigurationNameOrId`:
   *
   * @return successful operation
   */
  getGooglePlayApplicationConfiguration(params: GooglePlayApplicationConfigurationService.GetGooglePlayApplicationConfigurationParams): Observable<GooglePlayApplicationConfiguration> {
    return this.getGooglePlayApplicationConfigurationResponse(params).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Updates an existing Google Play Application profile if it is known to the server.
   * @param params The `GooglePlayApplicationConfigurationService.UpdateApplicationConfigurationParams` containing the following parameters:
   *
   * - `applicationNameOrId`:
   *
   * - `applicationConfigurationNameOrId`:
   *
   * - `body`:
   *
   * @return successful operation
   */
  updateApplicationConfigurationResponse(params: GooglePlayApplicationConfigurationService.UpdateApplicationConfigurationParams): Observable<StrictHttpResponse<GooglePlayApplicationConfiguration>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;


    __body = params.body;
    let req = new HttpRequest<any>(
      'PUT',
      this.rootUrl + `/application/${params.applicationNameOrId}/configuration/google_play/${params.applicationConfigurationNameOrId}`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<GooglePlayApplicationConfiguration>;
      })
    );
  }

  /**
   * Updates an existing Google Play Application profile if it is known to the server.
   * @param params The `GooglePlayApplicationConfigurationService.UpdateApplicationConfigurationParams` containing the following parameters:
   *
   * - `applicationNameOrId`:
   *
   * - `applicationConfigurationNameOrId`:
   *
   * - `body`:
   *
   * @return successful operation
   */
  updateApplicationConfiguration(params: GooglePlayApplicationConfigurationService.UpdateApplicationConfigurationParams): Observable<GooglePlayApplicationConfiguration> {
    return this.updateApplicationConfigurationResponse(params).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Deletes an existing Google Play Application profile if it is known to the server.
   * @param params The `GooglePlayApplicationConfigurationService.DeleteGooglePlayApplicationConfigurationParams` containing the following parameters:
   *
   * - `applicationNameOrId`:
   *
   * - `applicationConfigurationNameOrId`:
   */
  deleteGooglePlayApplicationConfigurationResponse(params: GooglePlayApplicationConfigurationService.DeleteGooglePlayApplicationConfigurationParams): Observable<StrictHttpResponse<void>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;


    let req = new HttpRequest<any>(
      'DELETE',
      this.rootUrl + `/application/${params.applicationNameOrId}/configuration/google_play/${params.applicationConfigurationNameOrId}`,
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
   * Deletes an existing Google Play Application profile if it is known to the server.
   * @param params The `GooglePlayApplicationConfigurationService.DeleteGooglePlayApplicationConfigurationParams` containing the following parameters:
   *
   * - `applicationNameOrId`:
   *
   * - `applicationConfigurationNameOrId`:
   */
  deleteGooglePlayApplicationConfiguration(params: GooglePlayApplicationConfigurationService.DeleteGooglePlayApplicationConfigurationParams): Observable<void> {
    return this.deleteGooglePlayApplicationConfigurationResponse(params).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Creates a new GooglePlay ApplicationConfiguration with the specific ID or application.
   * @param params The `GooglePlayApplicationConfigurationService.CreateGooglePlayApplicationConfigurationParams` containing the following parameters:
   *
   * - `applicationNameOrId`:
   *
   * - `body`:
   *
   * @return successful operation
   */
  createGooglePlayApplicationConfigurationResponse(params: GooglePlayApplicationConfigurationService.CreateGooglePlayApplicationConfigurationParams): Observable<StrictHttpResponse<GooglePlayApplicationConfiguration>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    __body = params.body;
    let req = new HttpRequest<any>(
      'POST',
      this.rootUrl + `/application/${params.applicationNameOrId}/configuration/google_play`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<GooglePlayApplicationConfiguration>;
      })
    );
  }

  /**
   * Creates a new GooglePlay ApplicationConfiguration with the specific ID or application.
   * @param params The `GooglePlayApplicationConfigurationService.CreateGooglePlayApplicationConfigurationParams` containing the following parameters:
   *
   * - `applicationNameOrId`:
   *
   * - `body`:
   *
   * @return successful operation
   */
  createGooglePlayApplicationConfiguration(params: GooglePlayApplicationConfigurationService.CreateGooglePlayApplicationConfigurationParams): Observable<GooglePlayApplicationConfiguration> {
    return this.createGooglePlayApplicationConfigurationResponse(params).pipe(
      __map(_r => _r.body)
    );
  }
}

module GooglePlayApplicationConfigurationService {

  /**
   * Parameters for getGooglePlayApplicationConfiguration
   */
  export interface GetGooglePlayApplicationConfigurationParams {
    applicationNameOrId: string;
    applicationConfigurationNameOrId: string;
  }

  /**
   * Parameters for updateApplicationConfiguration
   */
  export interface UpdateApplicationConfigurationParams {
    applicationNameOrId: string;
    applicationConfigurationNameOrId: string;
    body?: GooglePlayApplicationConfiguration;
  }

  /**
   * Parameters for deleteGooglePlayApplicationConfiguration
   */
  export interface DeleteGooglePlayApplicationConfigurationParams {
    applicationNameOrId: string;
    applicationConfigurationNameOrId: string;
  }

  /**
   * Parameters for createGooglePlayApplicationConfiguration
   */
  export interface CreateGooglePlayApplicationConfigurationParams {
    applicationNameOrId: string;
    body?: GooglePlayApplicationConfiguration;
  }
}

export { GooglePlayApplicationConfigurationService }
