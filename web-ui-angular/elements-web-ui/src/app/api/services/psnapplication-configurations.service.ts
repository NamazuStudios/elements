/* tslint:disable */
import { Injectable } from '@angular/core';
import { HttpClient, HttpRequest, HttpResponse, HttpHeaders, HttpParams } from '@angular/common/http';
import { BaseService } from '../base-service';
import { ApiConfiguration } from '../api-configuration';
import { StrictHttpResponse } from '../strict-http-response';
import { Observable } from 'rxjs';
import { map as __map, filter as __filter } from 'rxjs/operators';

import { PSNApplicationConfiguration } from '../models/psnapplication-configuration';
@Injectable({
  providedIn: 'root',
})
class PSNApplicationConfigurationsService extends BaseService {
  constructor(
    config: ApiConfiguration,
    http: HttpClient
  ) {
    super(config, http);
  }

  /**
   * Gets a single PSN application based on unique name or ID.
   * @param params The `PSNApplicationConfigurationsService.GetPSNApplicationConfigurationParams` containing the following parameters:
   *
   * - `applicationNameOrId`:
   *
   * - `applicationConfigurationNameOrId`:
   *
   * @return successful operation
   */
  getPSNApplicationConfigurationResponse(params: PSNApplicationConfigurationsService.GetPSNApplicationConfigurationParams): Observable<StrictHttpResponse<PSNApplicationConfiguration>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;


    let req = new HttpRequest<any>(
      'GET',
      this.rootUrl + `/application/${params.applicationNameOrId}/configuration/psn/${params.applicationConfigurationNameOrId}`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<PSNApplicationConfiguration>;
      })
    );
  }

  /**
   * Gets a single PSN application based on unique name or ID.
   * @param params The `PSNApplicationConfigurationsService.GetPSNApplicationConfigurationParams` containing the following parameters:
   *
   * - `applicationNameOrId`:
   *
   * - `applicationConfigurationNameOrId`:
   *
   * @return successful operation
   */
  getPSNApplicationConfiguration(params: PSNApplicationConfigurationsService.GetPSNApplicationConfigurationParams): Observable<PSNApplicationConfiguration> {
    return this.getPSNApplicationConfigurationResponse(params).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Updates an existing PSN Application profile if it is known to the server.
   * @param params The `PSNApplicationConfigurationsService.UpdateApplicationConfigurationParams` containing the following parameters:
   *
   * - `applicationNameOrId`:
   *
   * - `applicationConfigurationNameOrId`:
   *
   * - `body`:
   *
   * @return successful operation
   */
  updateApplicationConfigurationResponse(params: PSNApplicationConfigurationsService.UpdateApplicationConfigurationParams): Observable<StrictHttpResponse<PSNApplicationConfiguration>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;


    __body = params.body;
    let req = new HttpRequest<any>(
      'PUT',
      this.rootUrl + `/application/${params.applicationNameOrId}/configuration/psn/${params.applicationConfigurationNameOrId}`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<PSNApplicationConfiguration>;
      })
    );
  }

  /**
   * Updates an existing PSN Application profile if it is known to the server.
   * @param params The `PSNApplicationConfigurationsService.UpdateApplicationConfigurationParams` containing the following parameters:
   *
   * - `applicationNameOrId`:
   *
   * - `applicationConfigurationNameOrId`:
   *
   * - `body`:
   *
   * @return successful operation
   */
  updateApplicationConfiguration(params: PSNApplicationConfigurationsService.UpdateApplicationConfigurationParams): Observable<PSNApplicationConfiguration> {
    return this.updateApplicationConfigurationResponse(params).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Deletes an existing PSN Application profile if it is known to the server.
   * @param params The `PSNApplicationConfigurationsService.DeletePSNApplicationConfigurationParams` containing the following parameters:
   *
   * - `applicationNameOrId`:
   *
   * - `applicationConfigurationNameOrId`:
   */
  deletePSNApplicationConfigurationResponse(params: PSNApplicationConfigurationsService.DeletePSNApplicationConfigurationParams): Observable<StrictHttpResponse<void>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;


    let req = new HttpRequest<any>(
      'DELETE',
      this.rootUrl + `/application/${params.applicationNameOrId}/configuration/psn/${params.applicationConfigurationNameOrId}`,
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
   * Deletes an existing PSN Application profile if it is known to the server.
   * @param params The `PSNApplicationConfigurationsService.DeletePSNApplicationConfigurationParams` containing the following parameters:
   *
   * - `applicationNameOrId`:
   *
   * - `applicationConfigurationNameOrId`:
   */
  deletePSNApplicationConfiguration(params: PSNApplicationConfigurationsService.DeletePSNApplicationConfigurationParams): Observable<void> {
    return this.deletePSNApplicationConfigurationResponse(params).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Creates a new PSN ApplicationConfiguration with the specific ID or application.
   * @param params The `PSNApplicationConfigurationsService.CreatePSNApplicationConfigurationParams` containing the following parameters:
   *
   * - `applicationNameOrId`:
   *
   * - `body`:
   *
   * @return successful operation
   */
  createPSNApplicationConfigurationResponse(params: PSNApplicationConfigurationsService.CreatePSNApplicationConfigurationParams): Observable<StrictHttpResponse<PSNApplicationConfiguration>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    __body = params.body;
    let req = new HttpRequest<any>(
      'POST',
      this.rootUrl + `/application/${params.applicationNameOrId}/configuration/psn`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<PSNApplicationConfiguration>;
      })
    );
  }

  /**
   * Creates a new PSN ApplicationConfiguration with the specific ID or application.
   * @param params The `PSNApplicationConfigurationsService.CreatePSNApplicationConfigurationParams` containing the following parameters:
   *
   * - `applicationNameOrId`:
   *
   * - `body`:
   *
   * @return successful operation
   */
  createPSNApplicationConfiguration(params: PSNApplicationConfigurationsService.CreatePSNApplicationConfigurationParams): Observable<PSNApplicationConfiguration> {
    return this.createPSNApplicationConfigurationResponse(params).pipe(
      __map(_r => _r.body)
    );
  }
}

module PSNApplicationConfigurationsService {

  /**
   * Parameters for getPSNApplicationConfiguration
   */
  export interface GetPSNApplicationConfigurationParams {
    applicationNameOrId: string;
    applicationConfigurationNameOrId: string;
  }

  /**
   * Parameters for updateApplicationConfiguration
   */
  export interface UpdateApplicationConfigurationParams {
    applicationNameOrId: string;
    applicationConfigurationNameOrId: string;
    body?: PSNApplicationConfiguration;
  }

  /**
   * Parameters for deletePSNApplicationConfiguration
   */
  export interface DeletePSNApplicationConfigurationParams {
    applicationNameOrId: string;
    applicationConfigurationNameOrId: string;
  }

  /**
   * Parameters for createPSNApplicationConfiguration
   */
  export interface CreatePSNApplicationConfigurationParams {
    applicationNameOrId: string;
    body?: PSNApplicationConfiguration;
  }
}

export { PSNApplicationConfigurationsService }
