/* tslint:disable */
import { Injectable } from '@angular/core';
import { HttpClient, HttpRequest, HttpResponse, HttpHeaders, HttpParams } from '@angular/common/http';
import { BaseService } from '../base-service';
import { ApiConfiguration } from '../api-configuration';
import { StrictHttpResponse } from '../strict-http-response';
import { Observable } from 'rxjs';
import { map as __map, filter as __filter } from 'rxjs/operators';

import { GameOnPlayerTournamentEnterResponse } from '../models/game-on-player-tournament-enter-response';
import { GameOnPlayerTournamentEnterRequest } from '../models/game-on-player-tournament-enter-request';
import { GameOnTournamentDetail } from '../models/game-on-tournament-detail';
import { GameOnTournamentSummary } from '../models/game-on-tournament-summary';
import { GameOnFulfillPrizeListResponse } from '../models/game-on-fulfill-prize-list-response';
import { GameOnFulfillPrizeRequest } from '../models/game-on-fulfill-prize-request';
import { GameOnClaimPrizeListResponse } from '../models/game-on-claim-prize-list-response';
import { GameOnClaimPrizeListRequest } from '../models/game-on-claim-prize-list-request';
@Injectable({
  providedIn: 'root',
})
class GameOnPlayerTournamentService extends BaseService {
  constructor(
    config: ApiConfiguration,
    http: HttpClient
  ) {
    super(config, http);
  }

  /**
   * See:  https://developer.amazon.com/docs/gameon/game-api-ref.html#enter-player-tournament
   * @param params The `GameOnPlayerTournamentService.EnterTournamentParams` containing the following parameters:
   *
   * - `tournamentId`:
   *
   * - `body`:
   *
   * @return successful operation
   */
  enterTournamentResponse(params: GameOnPlayerTournamentService.EnterTournamentParams): Observable<StrictHttpResponse<GameOnPlayerTournamentEnterResponse>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    __body = params.body;
    let req = new HttpRequest<any>(
      'POST',
      this.rootUrl + `/game_on/tournament/player/${params.tournamentId}/entry`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<GameOnPlayerTournamentEnterResponse>;
      })
    );
  }

  /**
   * See:  https://developer.amazon.com/docs/gameon/game-api-ref.html#enter-player-tournament
   * @param params The `GameOnPlayerTournamentService.EnterTournamentParams` containing the following parameters:
   *
   * - `tournamentId`:
   *
   * - `body`:
   *
   * @return successful operation
   */
  enterTournament(params: GameOnPlayerTournamentService.EnterTournamentParams): Observable<GameOnPlayerTournamentEnterResponse> {
    return this.enterTournamentResponse(params).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Gets a single available tournament, specified by the identifier.  This will return 404 if the player is not eligible to because they have already entered.  See:  https://developer.amazon.com/docs/gameon/game-api-ref.html#get-player-tournament-details
   * @param params The `GameOnPlayerTournamentService.GetTournamentParams` containing the following parameters:
   *
   * - `tournamentId`: The player tournamet ID.
   *
   * - `playerAttributes`: Custom player attributes.
   *
   * - `os`:
   *
   * - `filterBy`:
   *
   * - `build`:
   *
   * @return successful operation
   */
  getTournamentResponse(params: GameOnPlayerTournamentService.GetTournamentParams): Observable<StrictHttpResponse<GameOnTournamentDetail>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    if (params.playerAttributes != null) __params = __params.set('playerAttributes', params.playerAttributes.toString());
    if (params.os != null) __params = __params.set('os', params.os.toString());
    if (params.filterBy != null) __params = __params.set('filterBy', params.filterBy.toString());
    if (params.build != null) __params = __params.set('build', params.build.toString());
    let req = new HttpRequest<any>(
      'GET',
      this.rootUrl + `/game_on/tournament/player/${params.tournamentId}`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<GameOnTournamentDetail>;
      })
    );
  }

  /**
   * Gets a single available tournament, specified by the identifier.  This will return 404 if the player is not eligible to because they have already entered.  See:  https://developer.amazon.com/docs/gameon/game-api-ref.html#get-player-tournament-details
   * @param params The `GameOnPlayerTournamentService.GetTournamentParams` containing the following parameters:
   *
   * - `tournamentId`: The player tournamet ID.
   *
   * - `playerAttributes`: Custom player attributes.
   *
   * - `os`:
   *
   * - `filterBy`:
   *
   * - `build`:
   *
   * @return successful operation
   */
  getTournament(params: GameOnPlayerTournamentService.GetTournamentParams): Observable<GameOnTournamentDetail> {
    return this.getTournamentResponse(params).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Gets all availble tournaments that the player can enter.  This automatically filters out any tournaments that the player has not alrady entered.  See: https://developer.amazon.com/docs/gameon/game-api-ref.html#get-player-tournaments
   * @param params The `GameOnPlayerTournamentService.GetTournamentsParams` containing the following parameters:
   *
   * - `playerAttributes`: Custom player attributes.
   *
   * - `period`:
   *
   * - `os`:
   *
   * - `filterBy`:
   *
   * - `build`:
   *
   * @return successful operation
   */
  getTournamentsResponse(params: GameOnPlayerTournamentService.GetTournamentsParams): Observable<StrictHttpResponse<Array<GameOnTournamentSummary>>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;
    if (params.playerAttributes != null) __params = __params.set('playerAttributes', params.playerAttributes.toString());
    if (params.period != null) __params = __params.set('period', params.period.toString());
    if (params.os != null) __params = __params.set('os', params.os.toString());
    if (params.filterBy != null) __params = __params.set('filterBy', params.filterBy.toString());
    if (params.build != null) __params = __params.set('build', params.build.toString());
    let req = new HttpRequest<any>(
      'GET',
      this.rootUrl + `/game_on/tournament/player`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<Array<GameOnTournamentSummary>>;
      })
    );
  }

  /**
   * Gets all availble tournaments that the player can enter.  This automatically filters out any tournaments that the player has not alrady entered.  See: https://developer.amazon.com/docs/gameon/game-api-ref.html#get-player-tournaments
   * @param params The `GameOnPlayerTournamentService.GetTournamentsParams` containing the following parameters:
   *
   * - `playerAttributes`: Custom player attributes.
   *
   * - `period`:
   *
   * - `os`:
   *
   * - `filterBy`:
   *
   * - `build`:
   *
   * @return successful operation
   */
  getTournaments(params: GameOnPlayerTournamentService.GetTournamentsParams): Observable<Array<GameOnTournamentSummary>> {
    return this.getTournamentsResponse(params).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * See: https://developer.amazon.com/docs/gameon/game-api-ref.html#fulfill-prizes
   * @param body undefined
   * @return successful operation
   */
  fulfillPrizesResponse(body?: GameOnFulfillPrizeRequest): Observable<StrictHttpResponse<GameOnFulfillPrizeListResponse>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;
    __body = body;
    let req = new HttpRequest<any>(
      'POST',
      this.rootUrl + `/game_on/prize/fulfill`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<GameOnFulfillPrizeListResponse>;
      })
    );
  }

  /**
   * See: https://developer.amazon.com/docs/gameon/game-api-ref.html#fulfill-prizes
   * @param body undefined
   * @return successful operation
   */
  fulfillPrizes(body?: GameOnFulfillPrizeRequest): Observable<GameOnFulfillPrizeListResponse> {
    return this.fulfillPrizesResponse(body).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * See: https://developer.amazon.com/docs/gameon/game-api-ref.html#claim-prizes
   * @param body undefined
   * @return successful operation
   */
  claimPrizesResponse(body?: GameOnClaimPrizeListRequest): Observable<StrictHttpResponse<GameOnClaimPrizeListResponse>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;
    __body = body;
    let req = new HttpRequest<any>(
      'POST',
      this.rootUrl + `/game_on/prize/claim`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<GameOnClaimPrizeListResponse>;
      })
    );
  }

  /**
   * See: https://developer.amazon.com/docs/gameon/game-api-ref.html#claim-prizes
   * @param body undefined
   * @return successful operation
   */
  claimPrizes(body?: GameOnClaimPrizeListRequest): Observable<GameOnClaimPrizeListResponse> {
    return this.claimPrizesResponse(body).pipe(
      __map(_r => _r.body)
    );
  }
}

module GameOnPlayerTournamentService {

  /**
   * Parameters for enterTournament
   */
  export interface EnterTournamentParams {
    tournamentId: string;
    body?: GameOnPlayerTournamentEnterRequest;
  }

  /**
   * Parameters for getTournament
   */
  export interface GetTournamentParams {

    /**
     * The player tournamet ID.
     */
    tournamentId: string;

    /**
     * Custom player attributes.
     */
    playerAttributes?: string;
    os?: 'fireos' | 'android' | 'ios' | 'pc' | 'mac' | 'linux' | 'xbox' | 'playstation' | 'nintendo' | 'html';
    filterBy?: 'live' | 'upcoming';
    build?: 'development' | 'release';
  }

  /**
   * Parameters for getTournaments
   */
  export interface GetTournamentsParams {

    /**
     * Custom player attributes.
     */
    playerAttributes?: string;
    period?: 'day' | 'week' | 'month' | 'all';
    os?: 'fireos' | 'android' | 'ios' | 'pc' | 'mac' | 'linux' | 'xbox' | 'playstation' | 'nintendo' | 'html';
    filterBy?: 'live' | 'upcoming';
    build?: 'development' | 'release';
  }
}

export { GameOnPlayerTournamentService }
