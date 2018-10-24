/* tslint:disable */
import { Injectable } from '@angular/core';
import { HttpClient, HttpRequest, HttpResponse, HttpHeaders, HttpParams } from '@angular/common/http';
import { BaseService } from '../base-service';
import { ApiConfiguration } from '../api-configuration';
import { StrictHttpResponse } from '../strict-http-response';
import { Observable } from 'rxjs';
import { map as __map, filter as __filter } from 'rxjs/operators';

import { GameOnTournamentEnterResponse } from '../models/game-on-tournament-enter-response';
import { GameOnTournamentEnterRequest } from '../models/game-on-tournament-enter-request';
import { GameOnTournamentDetail } from '../models/game-on-tournament-detail';
import { GameOnTournamentSummary } from '../models/game-on-tournament-summary';
@Injectable({
  providedIn: 'root',
})
class GameOnTournamentService extends BaseService {
  constructor(
    config: ApiConfiguration,
    http: HttpClient
  ) {
    super(config, http);
  }

  /**
   * See:  https://developer.amazon.com/docs/gameon/game-api-ref.html#enter-tournament
   * @param params The `GameOnTournamentService.EnterTournamentParams` containing the following parameters:
   *
   * - `tournamentId`:
   *
   * - `body`:
   *
   * @return successful operation
   */
  enterTournamentResponse(params: GameOnTournamentService.EnterTournamentParams): Observable<StrictHttpResponse<GameOnTournamentEnterResponse>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    __body = params.body;
    let req = new HttpRequest<any>(
      'POST',
      this.rootUrl + `/game_on/tournament/developer/${params.tournamentId}/entry`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<GameOnTournamentEnterResponse>;
      })
    );
  }

  /**
   * See:  https://developer.amazon.com/docs/gameon/game-api-ref.html#enter-tournament
   * @param params The `GameOnTournamentService.EnterTournamentParams` containing the following parameters:
   *
   * - `tournamentId`:
   *
   * - `body`:
   *
   * @return successful operation
   */
  enterTournament(params: GameOnTournamentService.EnterTournamentParams): Observable<GameOnTournamentEnterResponse> {
    return this.enterTournamentResponse(params).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Gets a single available tournament, specified by the identifier.  See:  https://developer.amazon.com/docs/gameon/game-api-ref.html#get-tournament-details
   * @param params The `GameOnTournamentService.GetTournamentParams` containing the following parameters:
   *
   * - `tournamentId`: The player tournamet ID.
   *
   * - `playerAttributes`: Custom player attributes.
   *
   * - `os`:
   *
   * - `filterBy`:
   *
   * - `eligibleOnly`: Set to true to filter tournaments that have not been entered already.
   *
   * - `build`:
   *
   * @return successful operation
   */
  getTournamentResponse(params: GameOnTournamentService.GetTournamentParams): Observable<StrictHttpResponse<GameOnTournamentDetail>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    if (params.playerAttributes != null) __params = __params.set('playerAttributes', params.playerAttributes.toString());
    if (params.os != null) __params = __params.set('os', params.os.toString());
    if (params.filterBy != null) __params = __params.set('filterBy', params.filterBy.toString());
    if (params.eligibleOnly != null) __params = __params.set('eligibleOnly', params.eligibleOnly.toString());
    if (params.build != null) __params = __params.set('build', params.build.toString());
    let req = new HttpRequest<any>(
      'GET',
      this.rootUrl + `/game_on/tournament/developer/${params.tournamentId}`,
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
   * Gets a single available tournament, specified by the identifier.  See:  https://developer.amazon.com/docs/gameon/game-api-ref.html#get-tournament-details
   * @param params The `GameOnTournamentService.GetTournamentParams` containing the following parameters:
   *
   * - `tournamentId`: The player tournamet ID.
   *
   * - `playerAttributes`: Custom player attributes.
   *
   * - `os`:
   *
   * - `filterBy`:
   *
   * - `eligibleOnly`: Set to true to filter tournaments that have not been entered already.
   *
   * - `build`:
   *
   * @return successful operation
   */
  getTournament(params: GameOnTournamentService.GetTournamentParams): Observable<GameOnTournamentDetail> {
    return this.getTournamentResponse(params).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Gets all availble tournaments that the player can enter.  This automatically filters out any tournaments that the player has not alrady entered.  See:  https://developer.amazon.com/docs/gameon/game-api-ref.html#get-tournaments
   * @param params The `GameOnTournamentService.GetTournamentsParams` containing the following parameters:
   *
   * - `playerAttributes`: Custom player attributes.
   *
   * - `period`:
   *
   * - `os`:
   *
   * - `filterBy`:
   *
   * - `eligibleOnly`: Set to true to filter tournaments that have not been entered already.
   *
   * - `build`:
   *
   * @return successful operation
   */
  getTournamentsResponse(params: GameOnTournamentService.GetTournamentsParams): Observable<StrictHttpResponse<Array<GameOnTournamentSummary>>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;
    if (params.playerAttributes != null) __params = __params.set('playerAttributes', params.playerAttributes.toString());
    if (params.period != null) __params = __params.set('period', params.period.toString());
    if (params.os != null) __params = __params.set('os', params.os.toString());
    if (params.filterBy != null) __params = __params.set('filterBy', params.filterBy.toString());
    if (params.eligibleOnly != null) __params = __params.set('eligibleOnly', params.eligibleOnly.toString());
    if (params.build != null) __params = __params.set('build', params.build.toString());
    let req = new HttpRequest<any>(
      'GET',
      this.rootUrl + `/game_on/tournament/developer`,
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
   * Gets all availble tournaments that the player can enter.  This automatically filters out any tournaments that the player has not alrady entered.  See:  https://developer.amazon.com/docs/gameon/game-api-ref.html#get-tournaments
   * @param params The `GameOnTournamentService.GetTournamentsParams` containing the following parameters:
   *
   * - `playerAttributes`: Custom player attributes.
   *
   * - `period`:
   *
   * - `os`:
   *
   * - `filterBy`:
   *
   * - `eligibleOnly`: Set to true to filter tournaments that have not been entered already.
   *
   * - `build`:
   *
   * @return successful operation
   */
  getTournaments(params: GameOnTournamentService.GetTournamentsParams): Observable<Array<GameOnTournamentSummary>> {
    return this.getTournamentsResponse(params).pipe(
      __map(_r => _r.body)
    );
  }
}

module GameOnTournamentService {

  /**
   * Parameters for enterTournament
   */
  export interface EnterTournamentParams {
    tournamentId: string;
    body?: GameOnTournamentEnterRequest;
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

    /**
     * Set to true to filter tournaments that have not been entered already.
     */
    eligibleOnly?: boolean;
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

    /**
     * Set to true to filter tournaments that have not been entered already.
     */
    eligibleOnly?: boolean;
    build?: 'development' | 'release';
  }
}

export { GameOnTournamentService }
