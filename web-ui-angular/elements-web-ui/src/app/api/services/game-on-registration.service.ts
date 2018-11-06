/* tslint:disable */
import { Injectable } from '@angular/core';
import { HttpClient, HttpRequest, HttpResponse, HttpHeaders, HttpParams } from '@angular/common/http';
import { BaseService } from '../base-service';
import { ApiConfiguration } from '../api-configuration';
import { StrictHttpResponse } from '../strict-http-response';
import { Observable } from 'rxjs';
import { map as __map, filter as __filter } from 'rxjs/operators';

import { GameOnRegistration } from '../models/game-on-registration';
import { PaginationGameOnRegistration } from '../models/pagination-game-on-registration';
@Injectable({
  providedIn: 'root',
})
class GameOnRegistrationService extends BaseService {
  constructor(
    config: ApiConfiguration,
    http: HttpClient
  ) {
    super(config, http);
  }

  /**
   * Gets a specific profile by profile ID.
   * @param gameOnRegistrationId undefined
   * @return successful operation
   */
  getGameOnRegistrationResponse(gameOnRegistrationId: string): Observable<StrictHttpResponse<GameOnRegistration>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    let req = new HttpRequest<any>(
      'GET',
      this.rootUrl + `/game_on/registration/${gameOnRegistrationId}`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<GameOnRegistration>;
      })
    );
  }

  /**
   * Gets a specific profile by profile ID.
   * @param gameOnRegistrationId undefined
   * @return successful operation
   */
  getGameOnRegistration(gameOnRegistrationId: string): Observable<GameOnRegistration> {
    return this.getGameOnRegistrationResponse(gameOnRegistrationId).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * @param gameOnRegistrationId undefined
   */
  deleteRegistrationResponse(gameOnRegistrationId: string): Observable<StrictHttpResponse<void>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    let req = new HttpRequest<any>(
      'DELETE',
      this.rootUrl + `/game_on/registration/${gameOnRegistrationId}`,
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
   * @param gameOnRegistrationId undefined
   */
  deleteRegistration(gameOnRegistrationId: string): Observable<void> {
    return this.deleteRegistrationResponse(gameOnRegistrationId).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Searches all GameOnRegistrations in the system and returning the metadata for all matches against the given search filter.
   * @param params The `GameOnRegistrationService.GetGameOnRegistrationsParams` containing the following parameters:
   *
   * - `search`:
   *
   * - `offset`:
   *
   * - `count`:
   *
   * @return successful operation
   */
  getGameOnRegistrationsResponse(params: GameOnRegistrationService.GetGameOnRegistrationsParams): Observable<StrictHttpResponse<PaginationGameOnRegistration>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;
    if (params.search != null) __params = __params.set('search', params.search.toString());
    if (params.offset != null) __params = __params.set('offset', params.offset.toString());
    if (params.count != null) __params = __params.set('count', params.count.toString());
    let req = new HttpRequest<any>(
      'GET',
      this.rootUrl + `/game_on/registration`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<PaginationGameOnRegistration>;
      })
    );
  }

  /**
   * Searches all GameOnRegistrations in the system and returning the metadata for all matches against the given search filter.
   * @param params The `GameOnRegistrationService.GetGameOnRegistrationsParams` containing the following parameters:
   *
   * - `search`:
   *
   * - `offset`:
   *
   * - `count`:
   *
   * @return successful operation
   */
  getGameOnRegistrations(params: GameOnRegistrationService.GetGameOnRegistrationsParams): Observable<PaginationGameOnRegistration> {
    return this.getGameOnRegistrationsResponse(params).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Supplying a GameOn Registration, this will create a new token based on the information supplied to the endpoint.  The response will contain the token as it was written to the database.  Only one GameOnRegistration may exist per Profile.  However a user may see several GameOnRegistration instances for their User.  See:  https://developer.amazon.com/docs/gameon/game-api-ref.html#register-player
   * @param body undefined
   * @return successful operation
   */
  createRegistrationResponse(body?: GameOnRegistration): Observable<StrictHttpResponse<GameOnRegistration>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;
    __body = body;
    let req = new HttpRequest<any>(
      'POST',
      this.rootUrl + `/game_on/registration`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<GameOnRegistration>;
      })
    );
  }

  /**
   * Supplying a GameOn Registration, this will create a new token based on the information supplied to the endpoint.  The response will contain the token as it was written to the database.  Only one GameOnRegistration may exist per Profile.  However a user may see several GameOnRegistration instances for their User.  See:  https://developer.amazon.com/docs/gameon/game-api-ref.html#register-player
   * @param body undefined
   * @return successful operation
   */
  createRegistration(body?: GameOnRegistration): Observable<GameOnRegistration> {
    return this.createRegistrationResponse(body).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Gets a specific profile by profile ID.
   * @return successful operation
   */
  getCurrentGameOnRegistrationResponse(): Observable<StrictHttpResponse<GameOnRegistration>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;
    let req = new HttpRequest<any>(
      'GET',
      this.rootUrl + `/game_on/registration/current`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<GameOnRegistration>;
      })
    );
  }

  /**
   * Gets a specific profile by profile ID.
   * @return successful operation
   */
  getCurrentGameOnRegistration(): Observable<GameOnRegistration> {
    return this.getCurrentGameOnRegistrationResponse().pipe(
      __map(_r => _r.body)
    );
  }
}

module GameOnRegistrationService {

  /**
   * Parameters for getGameOnRegistrations
   */
  export interface GetGameOnRegistrationsParams {
    search?: string;
    offset?: number;
    count?: number;
  }
}

export { GameOnRegistrationService }
