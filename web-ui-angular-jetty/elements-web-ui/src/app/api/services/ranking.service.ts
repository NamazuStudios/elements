/* tslint:disable */
import { Injectable } from '@angular/core';
import { HttpClient, HttpRequest, HttpResponse, HttpHeaders, HttpParams } from '@angular/common/http';
import { BaseService } from '../base-service';
import { ApiConfiguration } from '../api-configuration';
import { StrictHttpResponse } from '../strict-http-response';
import { Observable } from 'rxjs';
import { map as __map, filter as __filter } from 'rxjs/operators';

import { PaginationRank } from '../models/pagination-rank';
@Injectable({
  providedIn: 'root',
})
class RankingService extends BaseService {
  constructor(
    config: ApiConfiguration,
    http: HttpClient
  ) {
    super(config, http);
  }

  /**
   * Gets the current Profile's rank among all players for the particular leaderboard.
   * @param params The `RankingService.GetGlobalRankParams` containing the following parameters:
   *
   * - `leaderboardNameOrId`: Specifies the leaderboard name or ID.
   *
   * - `profileId`: The profile ID of the user.  If supplied this will skip ahead in the result set automatically allowing the player to find his or her rank.  Unlike other API methods, the supplied offset can be specified in reverse as a negative number allowing the user to be placed in the middle of the page.
   *
   * - `offset`: May be negative to place the requested player in the middle of the page.
   *
   * - `count`: The number of results to return in the page.
   *
   * @return successful operation
   */
  getGlobalRankResponse(params: RankingService.GetGlobalRankParams): Observable<StrictHttpResponse<PaginationRank>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    if (params.profileId != null) __params = __params.set('profileId', params.profileId.toString());
    if (params.offset != null) __params = __params.set('offset', params.offset.toString());
    if (params.count != null) __params = __params.set('count', params.count.toString());
    let req = new HttpRequest<any>(
      'GET',
      this.rootUrl + `/rank/global/${params.leaderboardNameOrId}`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<PaginationRank>;
      })
    );
  }

  /**
   * Gets the current Profile's rank among all players for the particular leaderboard.
   * @param params The `RankingService.GetGlobalRankParams` containing the following parameters:
   *
   * - `leaderboardNameOrId`: Specifies the leaderboard name or ID.
   *
   * - `profileId`: The profile ID of the user.  If supplied this will skip ahead in the result set automatically allowing the player to find his or her rank.  Unlike other API methods, the supplied offset can be specified in reverse as a negative number allowing the user to be placed in the middle of the page.
   *
   * - `offset`: May be negative to place the requested player in the middle of the page.
   *
   * - `count`: The number of results to return in the page.
   *
   * @return successful operation
   */
  getGlobalRank(params: RankingService.GetGlobalRankParams): Observable<PaginationRank> {
    return this.getGlobalRankResponse(params).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Gets the current Profile's rank among friends for the particular leaderboard.
   * @param params The `RankingService.GetRankAmongFriendsParams` containing the following parameters:
   *
   * - `leaderboardNameOrId`: Specifies the leaderboard name or ID.
   *
   * - `relative`: Indicates whether or not to fetch results in a relative fashion.
   *
   * - `offset`: May be negative to place the requested player in the middle of the page.
   *
   * - `count`: The number of results to return in the page.
   *
   * @return successful operation
   */
  getRankAmongFriendsResponse(params: RankingService.GetRankAmongFriendsParams): Observable<StrictHttpResponse<PaginationRank>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    if (params.relative != null) __params = __params.set('relative', params.relative.toString());
    if (params.offset != null) __params = __params.set('offset', params.offset.toString());
    if (params.count != null) __params = __params.set('count', params.count.toString());
    let req = new HttpRequest<any>(
      'GET',
      this.rootUrl + `/rank/friends/${params.leaderboardNameOrId}`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<PaginationRank>;
      })
    );
  }

  /**
   * Gets the current Profile's rank among friends for the particular leaderboard.
   * @param params The `RankingService.GetRankAmongFriendsParams` containing the following parameters:
   *
   * - `leaderboardNameOrId`: Specifies the leaderboard name or ID.
   *
   * - `relative`: Indicates whether or not to fetch results in a relative fashion.
   *
   * - `offset`: May be negative to place the requested player in the middle of the page.
   *
   * - `count`: The number of results to return in the page.
   *
   * @return successful operation
   */
  getRankAmongFriends(params: RankingService.GetRankAmongFriendsParams): Observable<PaginationRank> {
    return this.getRankAmongFriendsResponse(params).pipe(
      __map(_r => _r.body)
    );
  }
}

module RankingService {

  /**
   * Parameters for getGlobalRank
   */
  export interface GetGlobalRankParams {

    /**
     * Specifies the leaderboard name or ID.
     */
    leaderboardNameOrId: string;

    /**
     * The profile ID of the user.  If supplied this will skip ahead in the result set automatically allowing the player to find his or her rank.  Unlike other API methods, the supplied offset can be specified in reverse as a negative number allowing the user to be placed in the middle of the page.
     */
    profileId?: string;

    /**
     * May be negative to place the requested player in the middle of the page.
     */
    offset?: number;

    /**
     * The number of results to return in the page.
     */
    count?: number;
  }

  /**
   * Parameters for getRankAmongFriends
   */
  export interface GetRankAmongFriendsParams {

    /**
     * Specifies the leaderboard name or ID.
     */
    leaderboardNameOrId: string;

    /**
     * Indicates whether or not to fetch results in a relative fashion.
     */
    relative?: boolean;

    /**
     * May be negative to place the requested player in the middle of the page.
     */
    offset?: number;

    /**
     * The number of results to return in the page.
     */
    count?: number;
  }
}

export { RankingService }
