/* tslint:disable */
import { Injectable } from '@angular/core';
import { HttpClient, HttpRequest, HttpResponse, HttpHeaders, HttpParams } from '@angular/common/http';
import { BaseService } from '../base-service';
import { ApiConfiguration } from '../api-configuration';
import { StrictHttpResponse } from '../strict-http-response';
import { Observable } from 'rxjs';
import { map as __map, filter as __filter } from 'rxjs/operators';

import { PaginationFriend } from '../models/pagination-friend';
import { Friend } from '../models/friend';

@Injectable({
  providedIn: 'root',
})
class FriendsService extends BaseService {
  constructor(
    config: ApiConfiguration,
    http: HttpClient
  ) {
    super(config, http);
  }

  /**
   * Searches all friends in the system and returning the metadata for all matches against the given search filter.
   * @param params The `FriendsService.GetFriendsParams` containing the following parameters:
   *
   * - `search`:
   *
   * - `offset`:
   *
   * - `count`:
   *
   * @return successful operation
   */
  getFriendsResponse(params: FriendsService.GetFriendsParams): Observable<StrictHttpResponse<PaginationFriend>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;
    if (params.search != null) __params = __params.set('search', params.search.toString());
    if (params.offset != null) __params = __params.set('offset', params.offset.toString());
    if (params.count != null) __params = __params.set('count', params.count.toString());
    let req = new HttpRequest<any>(
      'GET',
      this.rootUrl + `/friend`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<PaginationFriend>;
      })
    );
  }

  /**
   * Searches all friends in the system and returning the metadata for all matches against the given search filter.
   * @param params The `FriendsService.GetFriendsParams` containing the following parameters:
   *
   * - `search`:
   *
   * - `offset`:
   *
   * - `count`:
   *
   * @return successful operation
   */
  getFriends(params: FriendsService.GetFriendsParams): Observable<PaginationFriend> {
    return this.getFriendsResponse(params).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Gets a specific friend using the ID of the friend.
   * @param friendId undefined
   * @return successful operation
   */
  getUserResponse(friendId: string): Observable<StrictHttpResponse<Friend>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    let req = new HttpRequest<any>(
      'GET',
      this.rootUrl + `/friend/${friendId}`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<Friend>;
      })
    );
  }

  /**
   * Gets a specific friend using the ID of the friend.
   * @param friendId undefined
   * @return successful operation
   */
  getUser(friendId: string): Observable<Friend> {
    return this.getUserResponse(friendId).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Once a friend is deleted, re-creating a friend will set the friendship status to outgoing.
   * @param friendId undefined
   */
  deleteRegistrationResponse(friendId: string): Observable<StrictHttpResponse<void>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    let req = new HttpRequest<any>(
      'DELETE',
      this.rootUrl + `/friend/${friendId}`,
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
   * Once a friend is deleted, re-creating a friend will set the friendship status to outgoing.
   * @param friendId undefined
   */
  deleteRegistration(friendId: string): Observable<void> {
    return this.deleteRegistrationResponse(friendId).pipe(
      __map(_r => _r.body)
    );
  }


}

module FriendsService {

  /**
   * Parameters for getFriends
   */
  export interface GetFriendsParams {
    search?: string;
    offset?: number;
    count?: number;
  }


}

export { FriendsService }
