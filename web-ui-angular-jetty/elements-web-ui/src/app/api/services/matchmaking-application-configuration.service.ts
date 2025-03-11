/* tslint:disable */
import { Injectable } from '@angular/core';
import { HttpClient, HttpRequest, HttpResponse, HttpHeaders, HttpParams } from '@angular/common/http';
import { BaseService } from '../base-service';
import { ApiConfiguration } from '../api-configuration';
import { StrictHttpResponse } from '../strict-http-response';
import { Observable } from 'rxjs';
import { map as __map, filter as __filter } from 'rxjs/operators';

import { MatchmakingApplicationConfiguration } from '../models/matchmaking-application-configuration';
@Injectable({
  providedIn: 'root',
})
class MatchmakingApplicationConfigurationService extends BaseService {
  constructor(
    config: ApiConfiguration,
    http: HttpClient
  ) {
    super(config, http);
  }

  /**
   * Gets a single iOS application based on unique name or ID.
   * @param params The `MatchmakingApplicationConfigurationService.GetMatchmakingApplicationConfigurationParams` containing the following parameters:
   *
   * - `applicationNameOrId`:
   *
   * - `applicationConfigurationNameOrId`:
   *
   * @return successful operation
   */
  getMatchmakingApplicationConfigurationResponse(params: MatchmakingApplicationConfigurationService.GetMatchmakingApplicationConfigurationParams): Observable<StrictHttpResponse<MatchmakingApplicationConfiguration>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;


    let req = new HttpRequest<any>(
      'GET',
      this.rootUrl + `/application/${params.applicationNameOrId}/configuration/matchmaking/${params.applicationConfigurationNameOrId}`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<MatchmakingApplicationConfiguration>;
      })
    );
  }

  /**
   * Gets a single iOS application based on unique name or ID.
   * @param params The `MatchmakingApplicationConfigurationService.GetMatchmakingApplicationConfigurationParams` containing the following parameters:
   *
   * - `applicationNameOrId`:
   *
   * - `applicationConfigurationNameOrId`:
   *
   * @return successful operation
   */
  getMatchmakingApplicationConfiguration(params: MatchmakingApplicationConfigurationService.GetMatchmakingApplicationConfigurationParams): Observable<MatchmakingApplicationConfiguration> {
    return this.getMatchmakingApplicationConfigurationResponse(params).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Updates an existing iOS Application profile if it is known to the server.
   * @param params The `MatchmakingApplicationConfigurationService.UpdateApplicationConfigurationParams` containing the following parameters:
   *
   * - `applicationNameOrId`:
   *
   * - `applicationConfigurationNameOrId`:
   *
   * - `body`:
   *
   * @return successful operation
   */
  updateApplicationConfigurationResponse(params: MatchmakingApplicationConfigurationService.UpdateApplicationConfigurationParams): Observable<StrictHttpResponse<MatchmakingApplicationConfiguration>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;


    __body = params.body;
    let req = new HttpRequest<any>(
      'PUT',
      this.rootUrl + `/application/${params.applicationNameOrId}/configuration/matchmaking/${params.applicationConfigurationNameOrId}`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<MatchmakingApplicationConfiguration>;
      })
    );
  }

  /**
   * Updates an existing iOS Application profile if it is known to the server.
   * @param params The `MatchmakingApplicationConfigurationService.UpdateApplicationConfigurationParams` containing the following parameters:
   *
   * - `applicationNameOrId`:
   *
   * - `applicationConfigurationNameOrId`:
   *
   * - `body`:
   *
   * @return successful operation
   */
  updateApplicationConfiguration(params: MatchmakingApplicationConfigurationService.UpdateApplicationConfigurationParams): Observable<MatchmakingApplicationConfiguration> {
    return this.updateApplicationConfigurationResponse(params).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Deletes an existing iOS Application profile if it is known to the server.
   * @param params The `MatchmakingApplicationConfigurationService.DeleteMatchmakingApplicationConfigurationParams` containing the following parameters:
   *
   * - `applicationNameOrId`:
   *
   * - `applicationConfigurationNameOrId`:
   */
  deleteMatchmakingApplicationConfigurationResponse(params: MatchmakingApplicationConfigurationService.DeleteMatchmakingApplicationConfigurationParams): Observable<StrictHttpResponse<void>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;


    let req = new HttpRequest<any>(
      'DELETE',
      this.rootUrl + `/application/${params.applicationNameOrId}/configuration/matchmaking/${params.applicationConfigurationNameOrId}`,
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
   * Deletes an existing iOS Application profile if it is known to the server.
   * @param params The `MatchmakingApplicationConfigurationService.DeleteMatchmakingApplicationConfigurationParams` containing the following parameters:
   *
   * - `applicationNameOrId`:
   *
   * - `applicationConfigurationNameOrId`:
   */
  deleteMatchmakingApplicationConfiguration(params: MatchmakingApplicationConfigurationService.DeleteMatchmakingApplicationConfigurationParams): Observable<void> {
    return this.deleteMatchmakingApplicationConfigurationResponse(params).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Creates a new iOS ApplicationConfiguration with the specific ID or application.
   * @param params The `MatchmakingApplicationConfigurationService.CreateMatchmakingApplicationConfigurationParams` containing the following parameters:
   *
   * - `applicationNameOrId`:
   *
   * - `body`:
   *
   * @return successful operation
   */
  createMatchmakingApplicationConfigurationResponse(params: MatchmakingApplicationConfigurationService.CreateMatchmakingApplicationConfigurationParams): Observable<StrictHttpResponse<MatchmakingApplicationConfiguration>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    __body = params.body;
    let req = new HttpRequest<any>(
      'POST',
      this.rootUrl + `/application/${params.applicationNameOrId}/configuration/matchmaking`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<MatchmakingApplicationConfiguration>;
      })
    );
  }

  /**
   * Creates a new iOS ApplicationConfiguration with the specific ID or application.
   * @param params The `MatchmakingApplicationConfigurationService.CreateMatchmakingApplicationConfigurationParams` containing the following parameters:
   *
   * - `applicationNameOrId`:
   *
   * - `body`:
   *
   * @return successful operation
   */
  createMatchmakingApplicationConfiguration(params: MatchmakingApplicationConfigurationService.CreateMatchmakingApplicationConfigurationParams): Observable<MatchmakingApplicationConfiguration> {
    return this.createMatchmakingApplicationConfigurationResponse(params).pipe(
      __map(_r => _r.body)
    );
  }
}

module MatchmakingApplicationConfigurationService {

  /**
   * Parameters for getMatchmakingApplicationConfiguration
   */
  export interface GetMatchmakingApplicationConfigurationParams {
    applicationNameOrId: string;
    applicationConfigurationNameOrId: string;
  }

  /**
   * Parameters for updateApplicationConfiguration
   */
  export interface UpdateApplicationConfigurationParams {
    applicationNameOrId: string;
    applicationConfigurationNameOrId: string;
    body?: MatchmakingApplicationConfiguration;
  }

  /**
   * Parameters for deleteMatchmakingApplicationConfiguration
   */
  export interface DeleteMatchmakingApplicationConfigurationParams {
    applicationNameOrId: string;
    applicationConfigurationNameOrId: string;
  }

  /**
   * Parameters for createMatchmakingApplicationConfiguration
   */
  export interface CreateMatchmakingApplicationConfigurationParams {
    applicationNameOrId: string;
    body?: MatchmakingApplicationConfiguration;
  }
}

export { MatchmakingApplicationConfigurationService }
