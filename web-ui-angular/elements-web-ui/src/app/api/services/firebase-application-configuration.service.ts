/* tslint:disable */
import { Injectable } from '@angular/core';
import { HttpClient, HttpRequest, HttpResponse, HttpHeaders, HttpParams } from '@angular/common/http';
import { BaseService } from '../base-service';
import { ApiConfiguration } from '../api-configuration';
import { StrictHttpResponse } from '../strict-http-response';
import { Observable } from 'rxjs';
import { map as __map, filter as __filter } from 'rxjs/operators';

import { FirebaseApplicationConfiguration } from '../models/firebase-application-configuration';

@Injectable({
  providedIn: 'root',
})
class FirebaseApplicationConfigurationService extends BaseService {
  constructor(
    config: ApiConfiguration,
    http: HttpClient
  ) {
    super(config, http);
  }

  /**
   * Gets a single Firebase application based on unique name or ID.
   * @param params The `FirebaseApplicationConfigurationService.GetFirebaseApplicationConfigurationParams` containing the following parameters:
   *
   * - `applicationNameOrId`:
   *
   * - `applicationConfigurationNameOrId`:
   *
   * @return successful operation
   */
  getFirebaseApplicationConfigurationResponse(params: FirebaseApplicationConfigurationService.GetFirebaseApplicationConfigurationParams): Observable<StrictHttpResponse<FirebaseApplicationConfiguration>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;


    let req = new HttpRequest<any>(
      'GET',
      this.rootUrl + `/application/${params.applicationNameOrId}/configuration/firebase/${params.applicationConfigurationNameOrId}`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<FirebaseApplicationConfiguration>;
      })
    );
  }

  /**
   * Gets a single Firebase application based on unique name or ID.
   * @param params The `FirebaseApplicationConfigurationService.GetFirebaseApplicationConfigurationParams` containing the following parameters:
   *
   * - `applicationNameOrId`:
   *
   * - `applicationConfigurationNameOrId`:
   *
   * @return successful operation
   */
  getFirebaseApplicationConfiguration(params: FirebaseApplicationConfigurationService.GetFirebaseApplicationConfigurationParams): Observable<FirebaseApplicationConfiguration> {
    return this.getFirebaseApplicationConfigurationResponse(params).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Updates an existing Firebase Application profile if it is known to the server.
   * @param params The `FirebaseApplicationConfigurationService.UpdateApplicationConfigurationParams` containing the following parameters:
   *
   * - `applicationNameOrId`:
   *
   * - `applicationConfigurationNameOrId`:
   *
   * - `body`:
   *
   * @return successful operation
   */
  updateApplicationConfigurationResponse(params: FirebaseApplicationConfigurationService.UpdateApplicationConfigurationParams): Observable<StrictHttpResponse<FirebaseApplicationConfiguration>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;


    __body = params.body;
    let req = new HttpRequest<any>(
      'PUT',
      this.rootUrl + `/application/${params.applicationNameOrId}/configuration/firebase/${params.applicationConfigurationNameOrId}`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<FirebaseApplicationConfiguration>;
      })
    );
  }

  /**
   * Updates an existing Firebase Application profile if it is known to the server.
   * @param params The `FirebaseApplicationConfigurationService.UpdateApplicationConfigurationParams` containing the following parameters:
   *
   * - `applicationNameOrId`:
   *
   * - `applicationConfigurationNameOrId`:
   *
   * - `body`:
   *
   * @return successful operation
   */
  updateApplicationConfiguration(params: FirebaseApplicationConfigurationService.UpdateApplicationConfigurationParams): Observable<FirebaseApplicationConfiguration> {
    return this.updateApplicationConfigurationResponse(params).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Deletes an existing Firebase Application profile if it is known to the server.
   * @param params The `FirebaseApplicationConfigurationService.DeleteFirebaseApplicationConfigurationParams` containing the following parameters:
   *
   * - `applicationNameOrId`:
   *
   * - `applicationConfigurationNameOrId`:
   */
  deleteFirebaseApplicationConfigurationResponse(params: FirebaseApplicationConfigurationService.DeleteFirebaseApplicationConfigurationParams): Observable<StrictHttpResponse<void>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;


    let req = new HttpRequest<any>(
      'DELETE',
      this.rootUrl + `/application/${params.applicationNameOrId}/configuration/firebase/${params.applicationConfigurationNameOrId}`,
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
   * Deletes an existing Firebase Application profile if it is known to the server.
   * @param params The `FirebaseApplicationConfigurationService.DeleteFirebaseApplicationConfigurationParams` containing the following parameters:
   *
   * - `applicationNameOrId`:
   *
   * - `applicationConfigurationNameOrId`:
   */
  deleteFirebaseApplicationConfiguration(params: FirebaseApplicationConfigurationService.DeleteFirebaseApplicationConfigurationParams): Observable<void> {
    return this.deleteFirebaseApplicationConfigurationResponse(params).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Creates a new Firebase ApplicationConfiguration with the specific ID or application.
   * @param params The `FirebaseApplicationConfigurationService.CreateFirebaseApplicationConfigurationParams` containing the following parameters:
   *
   * - `applicationNameOrId`:
   *
   * - `body`:
   *
   * @return successful operation
   */
  createFirebaseApplicationConfigurationResponse(params: FirebaseApplicationConfigurationService.CreateFirebaseApplicationConfigurationParams): Observable<StrictHttpResponse<FirebaseApplicationConfiguration>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    __body = params.body;
    let req = new HttpRequest<any>(
      'POST',
      this.rootUrl + `/application/${params.applicationNameOrId}/configuration/firebase`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<FirebaseApplicationConfiguration>;
      })
    );
  }

  /**
   * Creates a new Firebase ApplicationConfiguration with the specific ID or application.
   * @param params The `FirebaseApplicationConfigurationService.CreateFirebaseApplicationConfigurationParams` containing the following parameters:
   *
   * - `applicationNameOrId`:
   *
   * - `body`:
   *
   * @return successful operation
   */
  createFirebaseApplicationConfiguration(params: FirebaseApplicationConfigurationService.CreateFirebaseApplicationConfigurationParams): Observable<FirebaseApplicationConfiguration> {
    return this.createFirebaseApplicationConfigurationResponse(params).pipe(
      __map(_r => _r.body)
    );
  }

}

module FirebaseApplicationConfigurationService {

  /**
   * Parameters for getFirebaseApplicationConfiguration
   */
  export interface GetFirebaseApplicationConfigurationParams {
    applicationNameOrId: string;
    applicationConfigurationNameOrId: string;
  }

  /**
   * Parameters for updateApplicationConfiguration
   */
  export interface UpdateApplicationConfigurationParams {
    applicationNameOrId: string;
    applicationConfigurationNameOrId: string;
    body?: FirebaseApplicationConfiguration;
  }

  /**
   * Parameters for deleteFirebaseApplicationConfiguration
   */
  export interface DeleteFirebaseApplicationConfigurationParams {
    applicationNameOrId: string;
    applicationConfigurationNameOrId: string;
  }

  /**
   * Parameters for createFirebaseApplicationConfiguration
   */
  export interface CreateFirebaseApplicationConfigurationParams {
    applicationNameOrId: string;
    body?: FirebaseApplicationConfiguration;
  }

}

export { FirebaseApplicationConfigurationService }
