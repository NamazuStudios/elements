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
@Injectable({
  providedIn: 'root',
})
class GameOnEntryService extends BaseService {
  constructor(
    config: ApiConfiguration,
    http: HttpClient
  ) {
    super(config, http);
  }

  /**
   * Supplying a GameOn Registration, this will create a new token based on the information supplied to the endpoint.  The response will contain the token as it was written to the database.  Only one GameOnRegistration may exist per Profile.  However a user may see several GameOnRegistration instances for their User.  See:  https://developer.amazon.com/docs/gameon/game-api-ref.html#enter-tournament
   * @param params The `GameOnEntryService.EnterTournamentParams` containing the following parameters:
   *
   * - `tournamentId`:
   *
   * - `body`:
   *
   * @return successful operation
   */
  enterTournamentResponse(params: GameOnEntryService.EnterTournamentParams): Observable<StrictHttpResponse<GameOnTournamentEnterResponse>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    __body = params.body;
    let req = new HttpRequest<any>(
      'POST',
      this.rootUrl + `/game_on/tournament/${params.tournamentId}/entry`,
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
   * Supplying a GameOn Registration, this will create a new token based on the information supplied to the endpoint.  The response will contain the token as it was written to the database.  Only one GameOnRegistration may exist per Profile.  However a user may see several GameOnRegistration instances for their User.  See:  https://developer.amazon.com/docs/gameon/game-api-ref.html#enter-tournament
   * @param params The `GameOnEntryService.EnterTournamentParams` containing the following parameters:
   *
   * - `tournamentId`:
   *
   * - `body`:
   *
   * @return successful operation
   */
  enterTournament(params: GameOnEntryService.EnterTournamentParams): Observable<GameOnTournamentEnterResponse> {
    return this.enterTournamentResponse(params).pipe(
      __map(_r => _r.body)
    );
  }
}

module GameOnEntryService {

  /**
   * Parameters for enterTournament
   */
  export interface EnterTournamentParams {
    tournamentId: string;
    body?: GameOnTournamentEnterRequest;
  }
}

export { GameOnEntryService }
