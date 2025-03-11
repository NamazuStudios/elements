/* tslint:disable */
import { Injectable } from '@angular/core';
import { HttpClient, HttpRequest, HttpResponse, HttpHeaders, HttpParams } from '@angular/common/http';
import { BaseService } from '../base-service';
import { ApiConfiguration } from '../api-configuration';
import { StrictHttpResponse } from '../strict-http-response';
import { Observable } from 'rxjs';
import { map as __map, filter as __filter } from 'rxjs/operators';

import { MatchesPagination } from '../models/matches-pagination';
import { Match } from '../models/match';
@Injectable({
  providedIn: 'root',
})
class MatchesService extends BaseService {
  constructor(
    config: ApiConfiguration,
    http: HttpClient
  ) {
    super(config, http);
  }

  /**
   * Lists all matches available.  Under most circumstances, this will requires that a profile be made available to the request.  The server may choose to return an error if no suitable profile can be determined.
   * @param params The `MatchesService.GetMatchesParams` containing the following parameters:
   *
   * - `search`:
   *
   * - `offset`:
   *
   * - `count`:
   *
   * @return successful operation
   */
  getMatchesResponse(params: MatchesService.GetMatchesParams): Observable<StrictHttpResponse<MatchesPagination>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;
    if (params.search != null) __params = __params.set('search', params.search.toString());
    if (params.offset != null) __params = __params.set('offset', params.offset.toString());
    if (params.count != null) __params = __params.set('count', params.count.toString());
    let req = new HttpRequest<any>(
      'GET',
      this.rootUrl + `/match`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<MatchesPagination>;
      })
    );
  }

  /**
   * Lists all matches available.  Under most circumstances, this will requires that a profile be made available to the request.  The server may choose to return an error if no suitable profile can be determined.
   * @param params The `MatchesService.GetMatchesParams` containing the following parameters:
   *
   * - `search`:
   *
   * - `offset`:
   *
   * - `count`:
   *
   * @return successful operation
   */
  getMatches(params: MatchesService.GetMatchesParams): Observable<MatchesPagination> {
    return this.getMatchesResponse(params).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * This method accepts an instance of Match, effectively requesting that the server find a suitable opponent for a game.  As other suitable players create matches the created match object may be updated as a suitable opponent is found.  The client must poll matches for updates and react accordingly.
   * @param body undefined
   * @return successful operation
   */
  createMatchResponse(body?: Match): Observable<StrictHttpResponse<Match>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;
    __body = body;
    let req = new HttpRequest<any>(
      'POST',
      this.rootUrl + `/match`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<Match>;
      })
    );
  }

  /**
   * This method accepts an instance of Match, effectively requesting that the server find a suitable opponent for a game.  As other suitable players create matches the created match object may be updated as a suitable opponent is found.  The client must poll matches for updates and react accordingly.
   * @param body undefined
   * @return successful operation
   */
  createMatch(body?: Match): Observable<Match> {
    return this.createMatchResponse(body).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Gets a specific match given the match's unique ID.  Additionally, it is possible to instruct the API to wait for a period of time before sending the response.  The request will intentionally hang until the requested Match with ID has been updated in the database.
   * @param params The `MatchesService.GetMatchParams` containing the following parameters:
   *
   * - `matchId`:
   *
   * - `SocialEngine-LongPoll-Timeout`: The maximum amount time the server will wait until a request returns a default set of data for long polling.  Specifying a zero will request that the server wait indefinitely until responding.  Though, the server may enforce a practical upper limit on the amount of time it takes to return.  Omitting this header will prompt the server to treat the request as a normal request.
   *
   * @return successful operation
   */
  getMatchResponse(params: MatchesService.GetMatchParams): Observable<StrictHttpResponse<Match>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    if (params.SocialEngineLongPollTimeout != null) __headers = __headers.set('SocialEngine-LongPoll-Timeout', params.SocialEngineLongPollTimeout.toString());
    let req = new HttpRequest<any>(
      'GET',
      this.rootUrl + `/match/${params.matchId}`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<Match>;
      })
    );
  }

  /**
   * Gets a specific match given the match's unique ID.  Additionally, it is possible to instruct the API to wait for a period of time before sending the response.  The request will intentionally hang until the requested Match with ID has been updated in the database.
   * @param params The `MatchesService.GetMatchParams` containing the following parameters:
   *
   * - `matchId`:
   *
   * - `SocialEngine-LongPoll-Timeout`: The maximum amount time the server will wait until a request returns a default set of data for long polling.  Specifying a zero will request that the server wait indefinitely until responding.  Though, the server may enforce a practical upper limit on the amount of time it takes to return.  Omitting this header will prompt the server to treat the request as a normal request.
   *
   * @return successful operation
   */
  getMatch(params: MatchesService.GetMatchParams): Observable<Match> {
    return this.getMatchResponse(params).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Deletes and permanently removes the Match fromt he server.  This effectively will cancel any pending request for a match.  If a game is currently being played agaist the match, the server may reject the request to delete the match until the game concludes.
   * @param matchId undefined
   */
  deleteMatchResponse(matchId: string): Observable<StrictHttpResponse<void>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    let req = new HttpRequest<any>(
      'DELETE',
      this.rootUrl + `/match/${matchId}`,
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
   * Deletes and permanently removes the Match fromt he server.  This effectively will cancel any pending request for a match.  If a game is currently being played agaist the match, the server may reject the request to delete the match until the game concludes.
   * @param matchId undefined
   */
  deleteMatch(matchId: string): Observable<void> {
    return this.deleteMatchResponse(matchId).pipe(
      __map(_r => _r.body)
    );
  }
}

module MatchesService {

  /**
   * Parameters for getMatches
   */
  export interface GetMatchesParams {
    search?: string;
    offset?: number;
    count?: number;
  }

  /**
   * Parameters for getMatch
   */
  export interface GetMatchParams {
    matchId: string;

    /**
     * The maximum amount time the server will wait until a request returns a default set of data for long polling.  Specifying a zero will request that the server wait indefinitely until responding.  Though, the server may enforce a practical upper limit on the amount of time it takes to return.  Omitting this header will prompt the server to treat the request as a normal request.
     */
    SocialEngineLongPollTimeout?: number;
  }
}

export { MatchesService }
