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
import { PaginationFacebookFriend } from '../models/pagination-facebook-friend';
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

  /**
   * Returns the list of all Facebook friends who have not created a profile for the current application.
   * @param params The `FriendsService.GetUnivitedFacebookFriendsParams` containing the following parameters:
   *
   * - `offset`:
   *
   * - `count`:
   *
   * - `applicationConfiguration`:
   *
   * - `application`:
   *
   * - `Facebook-OAuthToken`:
   *
   * @return successful operation
   */
  getUnivitedFacebookFriendsResponse(params: FriendsService.GetUnivitedFacebookFriendsParams): Observable<StrictHttpResponse<PaginationFacebookFriend>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;
    if (params.offset != null) __params = __params.set('offset', params.offset.toString());
    if (params.count != null) __params = __params.set('count', params.count.toString());
    if (params.applicationConfiguration != null) __params = __params.set('applicationConfiguration', params.applicationConfiguration.toString());
    if (params.application != null) __params = __params.set('application', params.application.toString());
    if (params.FacebookOAuthToken != null) __headers = __headers.set('Facebook-OAuthToken', params.FacebookOAuthToken.toString());
    let req = new HttpRequest<any>(
      'GET',
      this.rootUrl + `/friend_uninvited/facebook`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<PaginationFacebookFriend>;
      })
    );
  }

  /**
   * Returns the list of all Facebook friends who have not created a profile for the current application.
   * @param params The `FriendsService.GetUnivitedFacebookFriendsParams` containing the following parameters:
   *
   * - `offset`:
   *
   * - `count`:
   *
   * - `applicationConfiguration`:
   *
   * - `application`:
   *
   * - `Facebook-OAuthToken`:
   *
   * @return successful operation
   */
  getUnivitedFacebookFriends(params: FriendsService.GetUnivitedFacebookFriendsParams): Observable<PaginationFacebookFriend> {
    return this.getUnivitedFacebookFriendsResponse(params).pipe(
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

  /**
   * Parameters for getUnivitedFacebookFriends
   */
  export interface GetUnivitedFacebookFriendsParams {
    offset?: number;
    count?: number;
    applicationConfiguration?: string;
    application?: string;
    FacebookOAuthToken?: string;
  }
}

export { FriendsService }
