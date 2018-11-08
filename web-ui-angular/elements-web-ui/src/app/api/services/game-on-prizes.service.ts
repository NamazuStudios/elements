/* tslint:disable */
import { Injectable } from '@angular/core';
import { HttpClient, HttpRequest, HttpResponse, HttpHeaders, HttpParams } from '@angular/common/http';
import { BaseService } from '../base-service';
import { ApiConfiguration } from '../api-configuration';
import { StrictHttpResponse } from '../strict-http-response';
import { Observable } from 'rxjs';
import { map as __map, filter as __filter } from 'rxjs/operators';

import { GameOnGetPrizeListResponse } from '../models/game-on-get-prize-list-response';
import { GameOnAddPrizeListResponse } from '../models/game-on-add-prize-list-response';
import { GameOnAddPrizeListRequest } from '../models/game-on-add-prize-list-request';
@Injectable({
  providedIn: 'root',
})
class GameOnPrizesService extends BaseService {
  constructor(
    config: ApiConfiguration,
    http: HttpClient
  ) {
    super(config, http);
  }

  /**
   * Lists all prizes using the admin APIs.  Requires Super-User Privileges.  See:  https://developer.amazon.com/docs/gameon/admin-api-ref.html#get-prize-list
   * @param params The `GameOnPrizesService.GetPrizesParams` containing the following parameters:
   *
   * - `configurationId`:
   *
   * - `applicationId`:
   *
   * @return successful operation
   */
  getPrizesResponse(params: GameOnPrizesService.GetPrizesParams): Observable<StrictHttpResponse<GameOnGetPrizeListResponse>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;


    let req = new HttpRequest<any>(
      'GET',
      this.rootUrl + `/game_on_admin/${params.applicationId}/${params.configurationId}/prizes`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<GameOnGetPrizeListResponse>;
      })
    );
  }

  /**
   * Lists all prizes using the admin APIs.  Requires Super-User Privileges.  See:  https://developer.amazon.com/docs/gameon/admin-api-ref.html#get-prize-list
   * @param params The `GameOnPrizesService.GetPrizesParams` containing the following parameters:
   *
   * - `configurationId`:
   *
   * - `applicationId`:
   *
   * @return successful operation
   */
  getPrizes(params: GameOnPrizesService.GetPrizesParams): Observable<GameOnGetPrizeListResponse> {
    return this.getPrizesResponse(params).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Adds all prizes using the admin APIs.  Requires Super-User Privileges.  See:  https://developer.amazon.com/docs/gameon/admin-api-ref.html#add-prize-list
   * @param params The `GameOnPrizesService.CreatePrizesParams` containing the following parameters:
   *
   * - `configurationId`:
   *
   * - `applicationId`:
   *
   * - `body`:
   *
   * @return successful operation
   */
  createPrizesResponse(params: GameOnPrizesService.CreatePrizesParams): Observable<StrictHttpResponse<GameOnAddPrizeListResponse>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;


    __body = params.body;
    let req = new HttpRequest<any>(
      'POST',
      this.rootUrl + `/game_on_admin/${params.applicationId}/${params.configurationId}/prizes`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<GameOnAddPrizeListResponse>;
      })
    );
  }

  /**
   * Adds all prizes using the admin APIs.  Requires Super-User Privileges.  See:  https://developer.amazon.com/docs/gameon/admin-api-ref.html#add-prize-list
   * @param params The `GameOnPrizesService.CreatePrizesParams` containing the following parameters:
   *
   * - `configurationId`:
   *
   * - `applicationId`:
   *
   * - `body`:
   *
   * @return successful operation
   */
  createPrizes(params: GameOnPrizesService.CreatePrizesParams): Observable<GameOnAddPrizeListResponse> {
    return this.createPrizesResponse(params).pipe(
      __map(_r => _r.body)
    );
  }
}

module GameOnPrizesService {

  /**
   * Parameters for getPrizes
   */
  export interface GetPrizesParams {
    configurationId: string;
    applicationId: string;
  }

  /**
   * Parameters for createPrizes
   */
  export interface CreatePrizesParams {
    configurationId: string;
    applicationId: string;
    body?: GameOnAddPrizeListRequest;
  }
}

export { GameOnPrizesService }
