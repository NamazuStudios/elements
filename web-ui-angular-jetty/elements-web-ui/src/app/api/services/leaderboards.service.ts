/* tslint:disable */
import { Injectable } from '@angular/core';
import { HttpClient, HttpRequest, HttpResponse, HttpHeaders} from '@angular/common/http';
import { BaseService } from '../base-service';
import { ApiConfiguration } from '../api-configuration';
import { StrictHttpResponse } from '../strict-http-response';
import { Observable } from 'rxjs';
import { map as __map, filter as __filter } from 'rxjs/operators';

import { Leaderboard } from '../models/leaderboard';
import { PaginationLeaderboard } from '../models/pagination-leaderboard';
@Injectable({
  providedIn: 'root',
})
class LeaderboardsService extends BaseService {
  constructor(
    config: ApiConfiguration,
    http: HttpClient
  ) {
    super(config, http);
  }

  /**
   * Gets the metadata for a single leaderboard.  This may include more specific details not availble in the bulk-get or fetch operation.
   * @param nameOrId undefined
   * @return successful operation
   */
  getLeaderboardResponse(nameOrId: string): Observable<StrictHttpResponse<Leaderboard>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    let req = new HttpRequest<any>(
      'GET',
      this.rootUrl + `/leaderboard/${nameOrId}`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<Leaderboard>;
      })
    );
  }

  /**
   * Gets the metadata for a single leaderboard.  This may include more specific details not availble in the bulk-get or fetch operation.
   * @param nameOrId undefined
   * @return successful operation
   */
  getLeaderboard(nameOrId: string): Observable<Leaderboard> {
    return this.getLeaderboardResponse(nameOrId).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Performs an update to an existing leaderboard known to the server.
   * @param params The `LeaderboardsService.UpdateLeaderboardParams` containing the following parameters:
   *
   * - `nameOrId`:
   *
   * - `body`:
   *
   * @return successful operation
   */
  updateLeaderboardResponse(params: LeaderboardsService.UpdateLeaderboardParams): Observable<StrictHttpResponse<Leaderboard>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    __body = params.body;
    let req = new HttpRequest<any>(
      'PUT',
      this.rootUrl + `/leaderboard/${params.nameOrId}`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<Leaderboard>;
      })
    );
  }

  /**
   * Performs an update to an existing leaderboard known to the server.
   * @param params The `LeaderboardsService.UpdateLeaderboardParams` containing the following parameters:
   *
   * - `nameOrId`:
   *
   * - `body`:
   *
   * @return successful operation
   */
  updateLeaderboard(params: LeaderboardsService.UpdateLeaderboardParams): Observable<Leaderboard> {
    return this.updateLeaderboardResponse(params).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Deletes a specific leaderboard known to the server.
   * @param nameOrId undefined
   */
  deleteLeaderboardResponse(nameOrId: string): Observable<StrictHttpResponse<void>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    let req = new HttpRequest<any>(
      'DELETE',
      this.rootUrl + `/leaderboard/${nameOrId}`,
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
   * Deletes a specific leaderboard known to the server.
   * @param nameOrId undefined
   */
  deleteLeaderboard(nameOrId: string): Observable<void> {
    return this.deleteLeaderboardResponse(nameOrId).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Performs a full-text search of all leaderboards known to the server.  As with other full-text endpoints this allows for pagination and offset.
   * @param params The `LeaderboardsService.GetLeaderboardsParams` containing the following parameters:
   *
   * - `search`:
   *
   * - `offset`:
   *
   * - `count`:
   *
   * @return successful operation
   */
  getLeaderboardsResponse(params: LeaderboardsService.GetLeaderboardsParams): Observable<StrictHttpResponse<PaginationLeaderboard>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;
    if (params.search != null) __params = __params.set('search', params.search.toString());
    if (params.offset != null) __params = __params.set('offset', params.offset.toString());
    if (params.count != null) __params = __params.set('count', params.count.toString());
    let req = new HttpRequest<any>(
      'GET',
      this.rootUrl + `/leaderboard`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<PaginationLeaderboard>;
      })
    );
  }

  /**
   * Performs a full-text search of all leaderboards known to the server.  As with other full-text endpoints this allows for pagination and offset.
   * @param params The `LeaderboardsService.GetLeaderboardsParams` containing the following parameters:
   *
   * - `search`:
   *
   * - `offset`:
   *
   * - `count`:
   *
   * @return successful operation
   */
  getLeaderboards(params: LeaderboardsService.GetLeaderboardsParams): Observable<PaginationLeaderboard> {
    return this.getLeaderboardsResponse(params).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Gets the metadata for a single leaderboard.  This may include more specific details not available in the bulk-get or fetch operation.
   * @param body undefined
   * @return successful operation
   */
  createLeaderboardResponse(body?: Leaderboard): Observable<StrictHttpResponse<Leaderboard>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;
    __body = body;
    let req = new HttpRequest<any>(
      'POST',
      this.rootUrl + `/leaderboard`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<Leaderboard>;
      })
    );
  }

  /**
   * Gets the metadata for a single leaderboard.  This may include more specific details not available in the bulk-get or fetch operation.
   * @param body undefined
   * @return successful operation
   */
  createLeaderboard(body?: Leaderboard): Observable<Leaderboard> {
    return this.createLeaderboardResponse(body).pipe(
      __map(_r => _r.body)
    );
  }
}

module LeaderboardsService {

  /**
   * Parameters for updateLeaderboard
   */
  export interface UpdateLeaderboardParams {
    nameOrId: string;
    body?: Leaderboard;
  }

  /**
   * Parameters for getLeaderboards
   */
  export interface GetLeaderboardsParams {
    search?: string;
    offset?: number;
    count?: number;
  }
}

export { LeaderboardsService }
