/* tslint:disable */
import { Injectable } from '@angular/core';
import { HttpClient, HttpRequest, HttpResponse, HttpHeaders, HttpParams } from '@angular/common/http';
import { BaseService } from '../base-service';
import { ApiConfiguration } from '../api-configuration';
import { StrictHttpResponse } from '../strict-http-response';
import { Observable } from 'rxjs';
import { map as __map, filter as __filter } from 'rxjs/operators';

import { Profile } from '../models/profile';
import { PaginationProfile } from '../models/pagination-profile';
@Injectable({
  providedIn: 'root',
})
class ProfilesService extends BaseService {
  constructor(
    config: ApiConfiguration,
    http: HttpClient
  ) {
    super(config, http);
  }

  /**
   * Gets a specific profile by profile ID.
   * @param name undefined
   * @return successful operation
   */
  getProfileResponse(name: string): Observable<StrictHttpResponse<Profile>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    let req = new HttpRequest<any>(
      'GET',
      this.rootUrl + `/profile/${name}`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<Profile>;
      })
    );
  }

  /**
   * Gets a specific profile by profile ID.
   * @param name undefined
   * @return successful operation
   */
  getProfile(name: string): Observable<Profile> {
    return this.getProfileResponse(name).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * This is a special endpoing which fetches the current Profile based on current auth credentials.  This considers the currently loggged-in Dser as well as the Application or Application Configuration against which the User is operating.  This may not be availble, in which case the appopraite error is rasied.
   * @return successful operation
   */
  getCurrentProfileResponse(): Observable<StrictHttpResponse<Profile>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;
    let req = new HttpRequest<any>(
      'GET',
      this.rootUrl + `/profile/current`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<Profile>;
      })
    );
  }

  /**
   * This is a special endpoing which fetches the current Profile based on current auth credentials.  This considers the currently loggged-in Dser as well as the Application or Application Configuration against which the User is operating.  This may not be availble, in which case the appopraite error is rasied.
   * @return successful operation
   */
  getCurrentProfile(): Observable<Profile> {
    return this.getCurrentProfileResponse().pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Suppplying a profile Object will attempt to update the profile.  The call will return the profile as it was written to the database.
   * @param params The `ProfilesService.UpdateProfileParams` containing the following parameters:
   *
   * - `profileId`:
   *
   * - `body`:
   *
   * @return successful operation
   */
  updateProfileResponse(params: ProfilesService.UpdateProfileParams): Observable<StrictHttpResponse<Profile>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    __body = params.body;
    let req = new HttpRequest<any>(
      'PUT',
      this.rootUrl + `/profile/${params.profileId}`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<Profile>;
      })
    );
  }

  /**
   * Suppplying a profile Object will attempt to update the profile.  The call will return the profile as it was written to the database.
   * @param params The `ProfilesService.UpdateProfileParams` containing the following parameters:
   *
   * - `profileId`:
   *
   * - `body`:
   *
   * @return successful operation
   */
  updateProfile(params: ProfilesService.UpdateProfileParams): Observable<Profile> {
    return this.updateProfileResponse(params).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Deletes and permanently removes the Profile from the server.  The server maykeep some record around to preserve relationships and references, but this profile will not be accessible again until it is recreated.
   * @param profileId undefined
   */
  deactivateProfileResponse(profileId: string): Observable<StrictHttpResponse<void>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    let req = new HttpRequest<any>(
      'DELETE',
      this.rootUrl + `/profile/${profileId}`,
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
   * Deletes and permanently removes the Profile from the server.  The server maykeep some record around to preserve relationships and references, but this profile will not be accessible again until it is recreated.
   * @param profileId undefined
   */
  deactivateProfile(profileId: string): Observable<void> {
    return this.deactivateProfileResponse(profileId).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Searches all users in the system and returning the metadata for all matches against the given search filter.
   * @param params The `ProfilesService.GetProfilesParams` containing the following parameters:
   *
   * - `search`:
   *
   * - `offset`:
   *
   * - `count`:
   *
   * @return successful operation
   */
  getProfilesResponse(params: ProfilesService.GetProfilesParams): Observable<StrictHttpResponse<PaginationProfile>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;
    if (params.search != null) __params = __params.set('search', params.search.toString());
    if (params.offset != null) __params = __params.set('offset', params.offset.toString());
    if (params.count != null) __params = __params.set('count', params.count.toString());
    let req = new HttpRequest<any>(
      'GET',
      this.rootUrl + `/profile`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<PaginationProfile>;
      })
    );
  }

  /**
   * Searches all users in the system and returning the metadata for all matches against the given search filter.
   * @param params The `ProfilesService.GetProfilesParams` containing the following parameters:
   *
   * - `search`:
   *
   * - `offset`:
   *
   * - `count`:
   *
   * @return successful operation
   */
  getProfiles(params: ProfilesService.GetProfilesParams): Observable<PaginationProfile> {
    return this.getProfilesResponse(params).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Supplying the user object, this will update the user with the new information supplied in the body of the request.  Optionally, the user's password may be provided.
   * @param body undefined
   * @return successful operation
   */
  createProfileResponse(body?: Profile): Observable<StrictHttpResponse<Profile>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;
    __body = body;
    let req = new HttpRequest<any>(
      'POST',
      this.rootUrl + `/profile`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<Profile>;
      })
    );
  }

  /**
   * Supplying the user object, this will update the user with the new information supplied in the body of the request.  Optionally, the user's password may be provided.
   * @param body undefined
   * @return successful operation
   */
  createProfile(body?: Profile): Observable<Profile> {
    return this.createProfileResponse(body).pipe(
      __map(_r => _r.body)
    );
  }
}

module ProfilesService {

  /**
   * Parameters for updateProfile
   */
  export interface UpdateProfileParams {
    profileId: string;
    body?: Profile;
  }

  /**
   * Parameters for getProfiles
   */
  export interface GetProfilesParams {
    search?: string;
    offset?: number;
    count?: number;
  }
}

export { ProfilesService }
