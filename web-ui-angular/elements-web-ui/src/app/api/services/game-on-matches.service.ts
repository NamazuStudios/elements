/* tslint:disable */
import { Injectable } from '@angular/core';
import { HttpClient, HttpRequest, HttpResponse, HttpHeaders, HttpParams } from '@angular/common/http';
import { BaseService } from '../base-service';
import { ApiConfiguration } from '../api-configuration';
import { StrictHttpResponse } from '../strict-http-response';
import { Observable } from 'rxjs';
import { map as __map, filter as __filter } from 'rxjs/operators';

import { GameOnMatchesAggregate } from '../models/game-on-matches-aggregate';
import { GameOnEnterMatchResponse } from '../models/game-on-enter-match-response';
import { GameOnEnterMatchRequest } from '../models/game-on-enter-match-request';
import { GameOnGetMatchLeaderboardResponse } from '../models/game-on-get-match-leaderboard-response';
import { GameOnMatchDetail } from '../models/game-on-match-detail';
@Injectable({
  providedIn: 'root',
})
class GameOnMatchesService extends BaseService {
  constructor(
    config: ApiConfiguration,
    http: HttpClient
  ) {
    super(config, http);
  }

  /**
   * See: https://developer.amazon.com/docs/gameon/game-api-ref.html#get-matches
   * @param params The `GameOnMatchesService.GetMatchesParams` containing the following parameters:
   *
   * - `playerAttributes`: Custom player attributes.
   *
   * - `period`:
   *
   * - `os`:
   *
   * - `matchType`:
   *
   * - `filterBy`:
   *
   * - `build`:
   *
   * @return successful operation
   */
  getMatchesResponse(params: GameOnMatchesService.GetMatchesParams): Observable<StrictHttpResponse<GameOnMatchesAggregate>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;
    if (params.playerAttributes != null) __params = __params.set('playerAttributes', params.playerAttributes.toString());
    if (params.period != null) __params = __params.set('period', params.period.toString());
    if (params.os != null) __params = __params.set('os', params.os.toString());
    if (params.matchType != null) __params = __params.set('matchType', params.matchType.toString());
    if (params.filterBy != null) __params = __params.set('filterBy', params.filterBy.toString());
    if (params.build != null) __params = __params.set('build', params.build.toString());
    let req = new HttpRequest<any>(
      'GET',
      this.rootUrl + `/game_on/match`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<GameOnMatchesAggregate>;
      })
    );
  }

  /**
   * See: https://developer.amazon.com/docs/gameon/game-api-ref.html#get-matches
   * @param params The `GameOnMatchesService.GetMatchesParams` containing the following parameters:
   *
   * - `playerAttributes`: Custom player attributes.
   *
   * - `period`:
   *
   * - `os`:
   *
   * - `matchType`:
   *
   * - `filterBy`:
   *
   * - `build`:
   *
   * @return successful operation
   */
  getMatches(params: GameOnMatchesService.GetMatchesParams): Observable<GameOnMatchesAggregate> {
    return this.getMatchesResponse(params).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * See: https://developer.amazon.com/docs/gameon/game-api-ref.html#enter-match
   * @param params The `GameOnMatchesService.EnterMatchParams` containing the following parameters:
   *
   * - `matchId`: The match ID.
   *
   * - `body`:
   *
   * @return successful operation
   */
  enterMatchResponse(params: GameOnMatchesService.EnterMatchParams): Observable<StrictHttpResponse<GameOnEnterMatchResponse>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    __body = params.body;
    let req = new HttpRequest<any>(
      'POST',
      this.rootUrl + `/game_on/match/${params.matchId}/enter`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<GameOnEnterMatchResponse>;
      })
    );
  }

  /**
   * See: https://developer.amazon.com/docs/gameon/game-api-ref.html#enter-match
   * @param params The `GameOnMatchesService.EnterMatchParams` containing the following parameters:
   *
   * - `matchId`: The match ID.
   *
   * - `body`:
   *
   * @return successful operation
   */
  enterMatch(params: GameOnMatchesService.EnterMatchParams): Observable<GameOnEnterMatchResponse> {
    return this.enterMatchResponse(params).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * See: https://developer.amazon.com/docs/gameon/game-api-ref.html#get-leaderboard
   * @param params The `GameOnMatchesService.GetLeaderboardParams` containing the following parameters:
   *
   * - `matchId`: The match ID.
   *
   * - `os`:
   *
   * - `limit`:
   *
   * - `currentPlayerNeighbors`:
   *
   * - `build`:
   *
   * @return successful operation
   */
  getLeaderboardResponse(params: GameOnMatchesService.GetLeaderboardParams): Observable<StrictHttpResponse<GameOnGetMatchLeaderboardResponse>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    if (params.os != null) __params = __params.set('os', params.os.toString());
    if (params.limit != null) __params = __params.set('limit', params.limit.toString());
    if (params.currentPlayerNeighbors != null) __params = __params.set('currentPlayerNeighbors', params.currentPlayerNeighbors.toString());
    if (params.build != null) __params = __params.set('build', params.build.toString());
    let req = new HttpRequest<any>(
      'GET',
      this.rootUrl + `/game_on/match/${params.matchId}/leaderboard`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<GameOnGetMatchLeaderboardResponse>;
      })
    );
  }

  /**
   * See: https://developer.amazon.com/docs/gameon/game-api-ref.html#get-leaderboard
   * @param params The `GameOnMatchesService.GetLeaderboardParams` containing the following parameters:
   *
   * - `matchId`: The match ID.
   *
   * - `os`:
   *
   * - `limit`:
   *
   * - `currentPlayerNeighbors`:
   *
   * - `build`:
   *
   * @return successful operation
   */
  getLeaderboard(params: GameOnMatchesService.GetLeaderboardParams): Observable<GameOnGetMatchLeaderboardResponse> {
    return this.getLeaderboardResponse(params).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * See: https://developer.amazon.com/docs/gameon/game-api-ref.html#get-match-details
   * @param params The `GameOnMatchesService.GetMatchParams` containing the following parameters:
   *
   * - `matchId`: The match ID.
   *
   * - `playerAttributes`: Custom player attributes.
   *
   * - `os`:
   *
   * - `build`:
   *
   * @return successful operation
   */
  getMatchResponse(params: GameOnMatchesService.GetMatchParams): Observable<StrictHttpResponse<GameOnMatchDetail>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    if (params.playerAttributes != null) __params = __params.set('playerAttributes', params.playerAttributes.toString());
    if (params.os != null) __params = __params.set('os', params.os.toString());
    if (params.build != null) __params = __params.set('build', params.build.toString());
    let req = new HttpRequest<any>(
      'GET',
      this.rootUrl + `/game_on/match/${params.matchId}`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<GameOnMatchDetail>;
      })
    );
  }

  /**
   * See: https://developer.amazon.com/docs/gameon/game-api-ref.html#get-match-details
   * @param params The `GameOnMatchesService.GetMatchParams` containing the following parameters:
   *
   * - `matchId`: The match ID.
   *
   * - `playerAttributes`: Custom player attributes.
   *
   * - `os`:
   *
   * - `build`:
   *
   * @return successful operation
   */
  getMatch(params: GameOnMatchesService.GetMatchParams): Observable<GameOnMatchDetail> {
    return this.getMatchResponse(params).pipe(
      __map(_r => _r.body)
    );
  }
}

module GameOnMatchesService {

  /**
   * Parameters for getMatches
   */
  export interface GetMatchesParams {

    /**
     * Custom player attributes.
     */
    playerAttributes?: string;
    period?: 'day' | 'week' | 'month' | 'all';
    os?: 'fireos' | 'android' | 'ios' | 'pc' | 'mac' | 'linux' | 'xbox' | 'playstation' | 'nintendo' | 'html';
    matchType?: 'developer' | 'player_generated' | 'all';
    filterBy?: 'claimed_prizes' | 'unclaimed_prizes' | 'fulfilled_prizes' | 'prizes_won' | 'no_prizes_won' | 'live' | 'all';
    build?: 'development' | 'release';
  }

  /**
   * Parameters for enterMatch
   */
  export interface EnterMatchParams {

    /**
     * The match ID.
     */
    matchId: string;
    body?: GameOnEnterMatchRequest;
  }

  /**
   * Parameters for getLeaderboard
   */
  export interface GetLeaderboardParams {

    /**
     * The match ID.
     */
    matchId: string;
    os?: 'fireos' | 'android' | 'ios' | 'pc' | 'mac' | 'linux' | 'xbox' | 'playstation' | 'nintendo' | 'html';
    limit?: number;
    currentPlayerNeighbors?: number;
    build?: 'development' | 'release';
  }

  /**
   * Parameters for getMatch
   */
  export interface GetMatchParams {

    /**
     * The match ID.
     */
    matchId: string;

    /**
     * Custom player attributes.
     */
    playerAttributes?: string;
    os?: 'fireos' | 'android' | 'ios' | 'pc' | 'mac' | 'linux' | 'xbox' | 'playstation' | 'nintendo' | 'html';
    build?: 'development' | 'release';
  }
}

export { GameOnMatchesService }
