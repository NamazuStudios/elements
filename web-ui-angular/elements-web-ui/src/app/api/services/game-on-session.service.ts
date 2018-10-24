/* tslint:disable */
import { Injectable } from '@angular/core';
import { HttpClient, HttpRequest, HttpResponse, HttpHeaders, HttpParams } from '@angular/common/http';
import { BaseService } from '../base-service';
import { ApiConfiguration } from '../api-configuration';
import { StrictHttpResponse } from '../strict-http-response';
import { Observable } from 'rxjs';
import { map as __map, filter as __filter } from 'rxjs/operators';

import { PaginationGameOnSession } from '../models/pagination-game-on-session';
import { GameOnSession } from '../models/game-on-session';
@Injectable({
  providedIn: 'root',
})
class GameOnSessionService extends BaseService {
  constructor(
    config: ApiConfiguration,
    http: HttpClient
  ) {
    super(config, http);
  }

  /**
   * Searches all GameOnSessions in the system and returning the metadata for all matches against the given search filter.
   * @param params The `GameOnSessionService.GetGameOnSessionsParams` containing the following parameters:
   *
   * - `search`:
   *
   * - `offset`:
   *
   * - `count`:
   *
   * @return successful operation
   */
  getGameOnSessionsResponse(params: GameOnSessionService.GetGameOnSessionsParams): Observable<StrictHttpResponse<PaginationGameOnSession>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;
    if (params.search != null) __params = __params.set('search', params.search.toString());
    if (params.offset != null) __params = __params.set('offset', params.offset.toString());
    if (params.count != null) __params = __params.set('count', params.count.toString());
    let req = new HttpRequest<any>(
      'GET',
      this.rootUrl + `/game_on/session`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<PaginationGameOnSession>;
      })
    );
  }

  /**
   * Searches all GameOnSessions in the system and returning the metadata for all matches against the given search filter.
   * @param params The `GameOnSessionService.GetGameOnSessionsParams` containing the following parameters:
   *
   * - `search`:
   *
   * - `offset`:
   *
   * - `count`:
   *
   * @return successful operation
   */
  getGameOnSessions(params: GameOnSessionService.GetGameOnSessionsParams): Observable<PaginationGameOnSession> {
    return this.getGameOnSessionsResponse(params).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Supplying a GameOn Session, this will create a new token based on the information supplied to the endpoint.  The response will contain the token as it was written to the database.  Only one GameOnSession may exist per Profile.  However a user may see several GameOnSession instances for their User.  See:  https://developer.amazon.com/docs/gameon/game-api-ref.html#authenticate-player
   * @param body undefined
   * @return successful operation
   */
  createSessionResponse(body?: GameOnSession): Observable<StrictHttpResponse<GameOnSession>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;
    __body = body;
    let req = new HttpRequest<any>(
      'POST',
      this.rootUrl + `/game_on/session`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<GameOnSession>;
      })
    );
  }

  /**
   * Supplying a GameOn Session, this will create a new token based on the information supplied to the endpoint.  The response will contain the token as it was written to the database.  Only one GameOnSession may exist per Profile.  However a user may see several GameOnSession instances for their User.  See:  https://developer.amazon.com/docs/gameon/game-api-ref.html#authenticate-player
   * @param body undefined
   * @return successful operation
   */
  createSession(body?: GameOnSession): Observable<GameOnSession> {
    return this.createSessionResponse(body).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Gets a specific profile by profile ID.
   * @param gameOnSessionId undefined
   * @return successful operation
   */
  getGameOnSessionResponse(gameOnSessionId: string): Observable<StrictHttpResponse<GameOnSession>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    let req = new HttpRequest<any>(
      'GET',
      this.rootUrl + `/game_on/session/${gameOnSessionId}`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<GameOnSession>;
      })
    );
  }

  /**
   * Gets a specific profile by profile ID.
   * @param gameOnSessionId undefined
   * @return successful operation
   */
  getGameOnSession(gameOnSessionId: string): Observable<GameOnSession> {
    return this.getGameOnSessionResponse(gameOnSessionId).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * @param gameOnSessionId undefined
   */
  deleteSessionResponse(gameOnSessionId: string): Observable<StrictHttpResponse<void>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    let req = new HttpRequest<any>(
      'DELETE',
      this.rootUrl + `/game_on/session/${gameOnSessionId}`,
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
   * @param gameOnSessionId undefined
   */
  deleteSession(gameOnSessionId: string): Observable<void> {
    return this.deleteSessionResponse(gameOnSessionId).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Gets a specific GameOn Session filtered by the supplied OS type.  This infers the current profile and guarantees that only one session is returned.  This avoisd the client needing to perform needless sifting through the results client side.
   * @param os undefined
   * @return successful operation
   */
  getCurrentGameOnSessionResponse(os?: 'fireos' | 'android' | 'ios' | 'pc' | 'mac' | 'linux' | 'xbox' | 'playstation' | 'nintendo' | 'html'): Observable<StrictHttpResponse<GameOnSession>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;
    if (os != null) __params = __params.set('os', os.toString());
    let req = new HttpRequest<any>(
      'GET',
      this.rootUrl + `/game_on/session/current`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<GameOnSession>;
      })
    );
  }

  /**
   * Gets a specific GameOn Session filtered by the supplied OS type.  This infers the current profile and guarantees that only one session is returned.  This avoisd the client needing to perform needless sifting through the results client side.
   * @param os undefined
   * @return successful operation
   */
  getCurrentGameOnSession(os?: 'fireos' | 'android' | 'ios' | 'pc' | 'mac' | 'linux' | 'xbox' | 'playstation' | 'nintendo' | 'html'): Observable<GameOnSession> {
    return this.getCurrentGameOnSessionResponse(os).pipe(
      __map(_r => _r.body)
    );
  }
}

module GameOnSessionService {

  /**
   * Parameters for getGameOnSessions
   */
  export interface GetGameOnSessionsParams {
    search?: string;
    offset?: number;
    count?: number;
  }
}

export { GameOnSessionService }
