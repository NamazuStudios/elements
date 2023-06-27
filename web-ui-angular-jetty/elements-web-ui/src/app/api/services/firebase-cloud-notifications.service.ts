/* tslint:disable */
import { Injectable } from '@angular/core';
import { HttpClient, HttpRequest, HttpResponse, HttpHeaders, HttpParams } from '@angular/common/http';
import { BaseService } from '../base-service';
import { ApiConfiguration } from '../api-configuration';
import { StrictHttpResponse } from '../strict-http-response';
import { Observable } from 'rxjs';
import { map as __map, filter as __filter } from 'rxjs/operators';

import { FCMRegistration } from '../models/fcmregistration';
@Injectable({
  providedIn: 'root',
})
class FirebaseCloudNotificationsService extends BaseService {
  constructor(
    config: ApiConfiguration,
    http: HttpClient
  ) {
    super(config, http);
  }

  /**
   * Supplying FCM registration token, this will update the token string with the supplied values.  Clients may update the same registration with a different token issued with Firebase if they wish to simply retain the association with the
   * @param params The `FirebaseCloudNotificationsService.UpdateRegistrationParams` containing the following parameters:
   *
   * - `fcmRegistrationId`:
   *
   * - `body`:
   *
   * @return successful operation
   */
  updateRegistrationResponse(params: FirebaseCloudNotificationsService.UpdateRegistrationParams): Observable<StrictHttpResponse<FCMRegistration>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    __body = params.body;
    let req = new HttpRequest<any>(
      'PUT',
      this.rootUrl + `/notification/fcm/${params.fcmRegistrationId}`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<FCMRegistration>;
      })
    );
  }

  /**
   * Supplying FCM registration token, this will update the token string with the supplied values.  Clients may update the same registration with a different token issued with Firebase if they wish to simply retain the association with the
   * @param params The `FirebaseCloudNotificationsService.UpdateRegistrationParams` containing the following parameters:
   *
   * - `fcmRegistrationId`:
   *
   * - `body`:
   *
   * @return successful operation
   */
  updateRegistration(params: FirebaseCloudNotificationsService.UpdateRegistrationParams): Observable<FCMRegistration> {
    return this.updateRegistrationResponse(params).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * @param fcmRegistrationId undefined
   */
  deleteRegistrationResponse(fcmRegistrationId: string): Observable<StrictHttpResponse<void>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    let req = new HttpRequest<any>(
      'DELETE',
      this.rootUrl + `/notification/fcm/${fcmRegistrationId}`,
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
   * @param fcmRegistrationId undefined
   */
  deleteRegistration(fcmRegistrationId: string): Observable<void> {
    return this.deleteRegistrationResponse(fcmRegistrationId).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Supplying FCM registration token, this will create a new token based on the information supplied to the endpoint.  The response will contain the token as it was written to the database.  Clients may subsequently update the token string with new values as they are issued by Firebase.
   * @param body undefined
   * @return successful operation
   */
  createRegistrationResponse(body?: FCMRegistration): Observable<StrictHttpResponse<FCMRegistration>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;
    __body = body;
    let req = new HttpRequest<any>(
      'POST',
      this.rootUrl + `/notification/fcm`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<FCMRegistration>;
      })
    );
  }

  /**
   * Supplying FCM registration token, this will create a new token based on the information supplied to the endpoint.  The response will contain the token as it was written to the database.  Clients may subsequently update the token string with new values as they are issued by Firebase.
   * @param body undefined
   * @return successful operation
   */
  createRegistration(body?: FCMRegistration): Observable<FCMRegistration> {
    return this.createRegistrationResponse(body).pipe(
      __map(_r => _r.body)
    );
  }
}

module FirebaseCloudNotificationsService {

  /**
   * Parameters for updateRegistration
   */
  export interface UpdateRegistrationParams {
    fcmRegistrationId: string;
    body?: FCMRegistration;
  }
}

export { FirebaseCloudNotificationsService }
