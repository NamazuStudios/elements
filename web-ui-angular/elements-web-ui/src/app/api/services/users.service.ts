/* tslint:disable */
import { Injectable } from '@angular/core';
import { HttpClient, HttpRequest, HttpResponse, HttpHeaders, HttpParams } from '@angular/common/http';
import { BaseService } from '../base-service';
import { ApiConfiguration } from '../api-configuration';
import { StrictHttpResponse } from '../strict-http-response';
import { Observable } from 'rxjs';
import { map as __map, filter as __filter } from 'rxjs/operators';

import { User } from '../models/user';
import { PaginationUser } from '../models/pagination-user';
@Injectable({
  providedIn: 'root',
})
class UsersService extends BaseService {
  constructor(
    config: ApiConfiguration,
    http: HttpClient
  ) {
    super(config, http);
  }

  /**
   * A special endpoint used to get the current user for the request.  The current user is typically associated with the session but may be derived any other way.  This is essentially an alias for using GET /user/myUserId
   * @return successful operation
   */
  getUserResponse(): Observable<StrictHttpResponse<User>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;
    let req = new HttpRequest<any>(
      'GET',
      this.rootUrl + `/user/me`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<User>;
      })
    );
  }

  /**
   * A special endpoint used to get the current user for the request.  The current user is typically associated with the session but may be derived any other way.  This is essentially an alias for using GET /user/myUserId
   * @return successful operation
   */
  getUser(): Observable<User> {
    return this.getUserResponse().pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Gets a specific user by email or unique user ID.
   * @param name undefined
   * @return successful operation
   */
  getUser_1Response(name: string): Observable<StrictHttpResponse<User>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    let req = new HttpRequest<any>(
      'GET',
      this.rootUrl + `/user/${name}`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<User>;
      })
    );
  }

  /**
   * Gets a specific user by email or unique user ID.
   * @param name undefined
   * @return successful operation
   */
  getUser_1(name: string): Observable<User> {
    return this.getUser_1Response(name).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Supplying the user object, this will update the user with the new information supplied in the body of the request.  Optionally, the user's password may be provided.
   * @param params The `UsersService.UpdateUserParams` containing the following parameters:
   *
   * - `name`:
   *
   * - `password`:
   *
   * - `body`:
   *
   * @return successful operation
   */
  updateUserResponse(params: UsersService.UpdateUserParams): Observable<StrictHttpResponse<User>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    //if (params.password != null) __params = __params.set('password', params.password.toString());
    __body = params.body;
    let req = new HttpRequest<any>(
      'PUT',
      this.rootUrl + `/user/${params.name}`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<User>;
      })
    );
  }

  /**
   * Supplying the user object, this will update the user with the new information supplied in the body of the request.  Optionally, the user's password may be provided.
   * @param params The `UsersService.UpdateUserParams` containing the following parameters:
   *
   * - `name`:
   *
   * - `body`:
   *
   * @return successful operation
   */
  updateUser(params: UsersService.UpdateUserParams): Observable<User> {
    return this.updateUserResponse(params).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Deletes and permanently removes the user from the server.  The server may keep some metadata as necessary to avoid data inconsistency.  However, the user has been deleted from the client standpoint and will not be accessible through any of the existing APIs.
   * @param name undefined
   */
  deactivateUserResponse(name: string): Observable<StrictHttpResponse<void>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    let req = new HttpRequest<any>(
      'DELETE',
      this.rootUrl + `/user/${name}`,
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
   * Deletes and permanently removes the user from the server.  The server may keep some metadata as necessary to avoid data inconsistency.  However, the user has been deleted from the client standpoint and will not be accessible through any of the existing APIs.
   * @param name undefined
   */
  deactivateUser(name: string): Observable<void> {
    return this.deactivateUserResponse(name).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Searches all users in the system and returning the metadata for all matches against the given search filter.
   * @param params The `UsersService.GetUsersParams` containing the following parameters:
   *
   * - `search`:
   *
   * - `offset`:
   *
   * - `count`:
   *
   * @return successful operation
   */
  getUsersResponse(params: UsersService.GetUsersParams): Observable<StrictHttpResponse<PaginationUser>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;
    if (params.search != null) __params = __params.set('search', params.search.toString());
    if (params.offset != null) __params = __params.set('offset', params.offset.toString());
    if (params.count != null) __params = __params.set('count', params.count.toString());
    let req = new HttpRequest<any>(
      'GET',
      this.rootUrl + `/user`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<PaginationUser>;
      })
    );
  }

  /**
   * Searches all users in the system and returning the metadata for all matches against the given search filter.
   * @param params The `UsersService.GetUsersParams` containing the following parameters:
   *
   * - `search`:
   *
   * - `offset`:
   *
   * - `count`:
   *
   * @return successful operation
   */
  getUsers(params: UsersService.GetUsersParams): Observable<PaginationUser> {
    return this.getUsersResponse(params).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Supplying the user object, this will update the user with the new information supplied in the body of the request.  Optionally, the user's password may be provided.
   * @param params The `UsersService.CreateUserParams` containing the following parameters:
   *
   * - `password`:
   *
   * - `body`:
   *
   * @return successful operation
   */
  createUserResponse(params: UsersService.CreateUserParams): Observable<StrictHttpResponse<User>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders({['content-type']:["application/json"]})
    let __body: any = null;

    __body = params.body;
    delete __body.id;
    let req = new HttpRequest<any>(
      'POST',
      this.rootUrl + `/user`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<User>;
      })
    );
  }

  /**
   * Supplying the user object, this will update the user with the new information supplied in the body of the request.  Optionally, the user's password may be provided.
   * @param params The `UsersService.CreateUserParams` containing the following parameters:
   *
   * - `password`:
   *
   * - `body`:
   *
   * @return successful operation
   */
  createUser(params: UsersService.CreateUserParams): Observable<User> {
    return this.createUserResponse(params).pipe(
      __map(_r => _r.body)
    );
  }
}

module UsersService {

  /**
   * Parameters for updateUser
   */
  export interface UpdateUserParams {
    name: string;
    body?: {name: string, email: string, password?: string, level: string};
  }

  /**
   * Parameters for getUsers
   */
  export interface GetUsersParams {
    search?: string;
    offset?: number;
    count?: number;
  }

  /**
   * Parameters for createUser
   */
  export interface CreateUserParams {
    password?: string;
    body?: User;
  }
}

export { UsersService }
