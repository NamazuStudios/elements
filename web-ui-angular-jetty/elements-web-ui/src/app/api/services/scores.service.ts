/* tslint:disable */
import { Injectable } from '@angular/core';
import { HttpClient, HttpRequest, HttpResponse, HttpHeaders, HttpParams } from '@angular/common/http';
import { BaseService } from '../base-service';
import { ApiConfiguration } from '../api-configuration';
import { StrictHttpResponse } from '../strict-http-response';
import { Observable } from 'rxjs';
import { map as __map, filter as __filter } from 'rxjs/operators';

import { Score } from '../models/score';
@Injectable({
  providedIn: 'root',
})
class ScoresService extends BaseService {
  constructor(
    config: ApiConfiguration,
    http: HttpClient
  ) {
    super(config, http);
  }

  /**
   * Posts a single score for the currently logged-in profile. Conceptually, this is creationg a new resource, however the server may opt to overwrite the existing identifier if it sees fit.
   * @param params The `ScoresService.CreateScoreParams` containing the following parameters:
   *
   * - `leaderboardNameOrId`: The name or id of the leaderboard.
   *
   * - `body`:
   *
   * @return successful operation
   */
  createScoreResponse(params: ScoresService.CreateScoreParams): Observable<StrictHttpResponse<Score>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    __body = params.body;
    let req = new HttpRequest<any>(
      'POST',
      this.rootUrl + `/score/${params.leaderboardNameOrId}`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<Score>;
      })
    );
  }

  /**
   * Posts a single score for the currently logged-in profile. Conceptually, this is creationg a new resource, however the server may opt to overwrite the existing identifier if it sees fit.
   * @param params The `ScoresService.CreateScoreParams` containing the following parameters:
   *
   * - `leaderboardNameOrId`: The name or id of the leaderboard.
   *
   * - `body`:
   *
   * @return successful operation
   */
  createScore(params: ScoresService.CreateScoreParams): Observable<Score> {
    return this.createScoreResponse(params).pipe(
      __map(_r => _r.body)
    );
  }
}

module ScoresService {

  /**
   * Parameters for createScore
   */
  export interface CreateScoreParams {

    /**
     * The name or id of the leaderboard.
     */
    leaderboardNameOrId: string;
    body?: Score;
  }
}

export { ScoresService }
