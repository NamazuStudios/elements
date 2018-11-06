/* tslint:disable */
import { Injectable } from '@angular/core';
import { HttpClient, HttpRequest, HttpResponse, HttpHeaders, HttpParams } from '@angular/common/http';
import { BaseService } from '../base-service';
import { ApiConfiguration } from '../api-configuration';
import { StrictHttpResponse } from '../strict-http-response';
import { Observable } from 'rxjs';
import { map as __map, filter as __filter } from 'rxjs/operators';

import { IosApplicationConfiguration } from '../models/ios-application-configuration';
@Injectable({
  providedIn: 'root',
})
class IOSApplicationConfigurationService extends BaseService {
  constructor(
    config: ApiConfiguration,
    http: HttpClient
  ) {
    super(config, http);
  }

  /**
   * Gets a single iOS application based on unique name or ID.
   * @param params The `IOSApplicationConfigurationService.GetIosApplicationConfigurationParams` containing the following parameters:
   *
   * - `applicationNameOrId`:
   *
   * - `applicationConfigurationNameOrId`:
   *
   * @return successful operation
   */
  getIosApplicationConfigurationResponse(params: IOSApplicationConfigurationService.GetIosApplicationConfigurationParams): Observable<StrictHttpResponse<IosApplicationConfiguration>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;


    let req = new HttpRequest<any>(
      'GET',
      this.rootUrl + `/application/${params.applicationNameOrId}/configuration/ios/${params.applicationConfigurationNameOrId}`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<IosApplicationConfiguration>;
      })
    );
  }

  /**
   * Gets a single iOS application based on unique name or ID.
   * @param params The `IOSApplicationConfigurationService.GetIosApplicationConfigurationParams` containing the following parameters:
   *
   * - `applicationNameOrId`:
   *
   * - `applicationConfigurationNameOrId`:
   *
   * @return successful operation
   */
  getIosApplicationConfiguration(params: IOSApplicationConfigurationService.GetIosApplicationConfigurationParams): Observable<IosApplicationConfiguration> {
    return this.getIosApplicationConfigurationResponse(params).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Updates an existing iOS Application profile if it is known to the server.
   * @param params The `IOSApplicationConfigurationService.UpdateApplicationConfigurationParams` containing the following parameters:
   *
   * - `applicationNameOrId`:
   *
   * - `applicationConfigurationNameOrId`:
   *
   * - `body`:
   *
   * @return successful operation
   */
  updateApplicationConfigurationResponse(params: IOSApplicationConfigurationService.UpdateApplicationConfigurationParams): Observable<StrictHttpResponse<IosApplicationConfiguration>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;


    __body = params.body;
    let req = new HttpRequest<any>(
      'PUT',
      this.rootUrl + `/application/${params.applicationNameOrId}/configuration/ios/${params.applicationConfigurationNameOrId}`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<IosApplicationConfiguration>;
      })
    );
  }

  /**
   * Updates an existing iOS Application profile if it is known to the server.
   * @param params The `IOSApplicationConfigurationService.UpdateApplicationConfigurationParams` containing the following parameters:
   *
   * - `applicationNameOrId`:
   *
   * - `applicationConfigurationNameOrId`:
   *
   * - `body`:
   *
   * @return successful operation
   */
  updateApplicationConfiguration(params: IOSApplicationConfigurationService.UpdateApplicationConfigurationParams): Observable<IosApplicationConfiguration> {
    return this.updateApplicationConfigurationResponse(params).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Deletes an existing iOS Application profile if it is known to the server.
   * @param params The `IOSApplicationConfigurationService.DeleteIosApplicationConfigurationParams` containing the following parameters:
   *
   * - `applicationNameOrId`:
   *
   * - `applicationConfigurationNameOrId`:
   */
  deleteIosApplicationConfigurationResponse(params: IOSApplicationConfigurationService.DeleteIosApplicationConfigurationParams): Observable<StrictHttpResponse<void>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;


    let req = new HttpRequest<any>(
      'DELETE',
      this.rootUrl + `/application/${params.applicationNameOrId}/configuration/ios/${params.applicationConfigurationNameOrId}`,
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
   * @param params The `IOSApplicationConfigurationService.DeleteIosApplicationConfigurationParams` containing the following parameters:
   *
   * - `applicationNameOrId`:
   *
   * - `applicationConfigurationNameOrId`:
   */
  deleteIosApplicationConfiguration(params: IOSApplicationConfigurationService.DeleteIosApplicationConfigurationParams): Observable<void> {
    return this.deleteIosApplicationConfigurationResponse(params).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Creates a new iOS ApplicationConfiguration with the specific ID or application.
   * @param params The `IOSApplicationConfigurationService.CreateIosApplicationConfigurationParams` containing the following parameters:
   *
   * - `applicationNameOrId`:
   *
   * - `body`:
   *
   * @return successful operation
   */
  createIosApplicationConfigurationResponse(params: IOSApplicationConfigurationService.CreateIosApplicationConfigurationParams): Observable<StrictHttpResponse<IosApplicationConfiguration>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    __body = params.body;
    let req = new HttpRequest<any>(
      'POST',
      this.rootUrl + `/application/${params.applicationNameOrId}/configuration/ios`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<IosApplicationConfiguration>;
      })
    );
  }

  /**
   * Creates a new iOS ApplicationConfiguration with the specific ID or application.
   * @param params The `IOSApplicationConfigurationService.CreateIosApplicationConfigurationParams` containing the following parameters:
   *
   * - `applicationNameOrId`:
   *
   * - `body`:
   *
   * @return successful operation
   */
  createIosApplicationConfiguration(params: IOSApplicationConfigurationService.CreateIosApplicationConfigurationParams): Observable<IosApplicationConfiguration> {
    return this.createIosApplicationConfigurationResponse(params).pipe(
      __map(_r => _r.body)
    );
  }
}

module IOSApplicationConfigurationService {

  /**
   * Parameters for getIosApplicationConfiguration
   */
  export interface GetIosApplicationConfigurationParams {
    applicationNameOrId: string;
    applicationConfigurationNameOrId: string;
  }

  /**
   * Parameters for updateApplicationConfiguration
   */
  export interface UpdateApplicationConfigurationParams {
    applicationNameOrId: string;
    applicationConfigurationNameOrId: string;
    body?: IosApplicationConfiguration;
  }

  /**
   * Parameters for deleteIosApplicationConfiguration
   */
  export interface DeleteIosApplicationConfigurationParams {
    applicationNameOrId: string;
    applicationConfigurationNameOrId: string;
  }

  /**
   * Parameters for createIosApplicationConfiguration
   */
  export interface CreateIosApplicationConfigurationParams {
    applicationNameOrId: string;
    body?: IosApplicationConfiguration;
  }
}

export { IOSApplicationConfigurationService }
