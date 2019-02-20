/* tslint:disable */
import { Injectable } from '@angular/core';
import {ApiConfiguration} from '../api-configuration';
import {HttpClient, HttpHeaders, HttpRequest, HttpResponse} from '@angular/common/http';
import {BaseService} from '../base-service';
import {Observable} from 'rxjs';
import {StrictHttpResponse} from '../strict-http-response';
import {PaginationItem} from '../models/pagination-item';
import {filter as __filter, map as __map} from 'rxjs/operators';
import {Item} from '../models/item';
import {Mission} from '../models';

@Injectable({
  providedIn: 'root'
})
export class MissionsService extends BaseService {
  constructor(
    config: ApiConfiguration,
    http: HttpClient
  ) {
    super(config, http);
  }

  /**
   * Searches all missions and returns all matching missions, filtered by the passed in search parameters.
   * If multiple tags are specified,then all items that contain an least one of the passed in tags are returned.
   * @param params The 'MissionsServce.GetMissionsParams' containing the following parameters:
   *
   * - 'tags':
   *
   * - 'search':
   *
   * - 'offset':
   *
   * - 'count':
   *
   * @return successful operation
   */
  getMissionsResponse(params: MissionsService.GetMissionsParams): Observable<StrictHttpResponse<PaginationItem>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;
    (params.tags || []).forEach((val, index) => {if (val != null) __params = __params.append('tags', val.toString())});
    if (params.search != null) __params = __params.set('search', params.search.toString());
    if (params.offset != null) __params = __params.set('offset', params.offset.toString());
    if (params.count != null) __params = __params.set('count', params.count.toString());
    let req = new HttpRequest<any>(
      'GET',
      this.rootUrl + `/mission`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<PaginationItem>;
      })
    );
  }

  /**
   * Searches all missions and returns all matching missions, filtered by the passed in search parameters.
   * If multiple tags are specified, then all missions that contain at least one of the passed in tags is returned.
   * @param params The `MissionsService.GetMissionsParams` containing the following parameters:
   *
   * - `tags`:
   *
   * - `search`:
   *
   * - `offset`:
   *
   * - `count`:
   *
   * @return successful operation
   */
  getMissions(params: MissionsService.GetMissionsParams): Observable<PaginationItem> {
    return this.getMissionsResponse(params).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Supplying a mission object, this will create a new mission with a newly assigned unique id.
   * The Mission representation returned in the response body is a representation of the Mission as persisted with a unique identifier
   * signed and with its fields properly normalized.  The supplied mission object submitted with the request must have a name property
   * that is unique across all missions.
   * @param body undefined
   * @return successful operation
   */
  createMissionResponse(body?: Mission): Observable<StrictHttpResponse<Mission>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;
    __body = body;

    let req = new HttpRequest<any>(
      'POST',
      this.rootUrl + `/mission`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      }
    );

    return this.http.request<any>(req).pipe(
      __filter(_r => {
        //console.log(_r);
        return _r instanceof HttpResponse;
      }),
      __map((_r: HttpResponse<any>) => {
        //console.log(_r);
        return _r as StrictHttpResponse<Item>;
      })
    );
  }

  /**
   * Supplying a mission object, this will create a new mission with a newly assigned unique id.
   * The Mission representation returned in the response body is a representation of the Mission as persisted with a unique identifier
   * signed and with its fields properly normalized.  The supplied mission object submitted with the request must have a name property
   * that is unique across all missions.
   * @param body undefined
   * @return successful operation
   */
  createMission(body?: Mission): Observable<Mission> {
    return this.createMissionResponse(body).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Looks up a mission by the passed in identifier
   * @param identifier undefined
   * @return successful operation
   */
  getMissionByIdentifierResponse(identifier: string): Observable<StrictHttpResponse<Item>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    let req = new HttpRequest<any>(
      'GET',
      this.rootUrl + `/mission/${identifier}`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<Item>;
      })
    );
  }

  /**
   * Looks up a mission by the passed in identifier
   * @param identifier undefined
   * @return successful operation
   */
  getMissionByIdentifier(identifier: string): Observable<Item> {
    return this.getMissionByIdentifierResponse(identifier).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Supplying a mission, this will update the Mission identified by the identifier in the path with contents from the passed in request body.
   * @param params The `MissionsService.UpdateMissionParams` containing the following parameters:
   *
   * - `identifier`:
   *
   * - `body`:
   *
   * @return successful operation
   */
  updateMissionResponse(params: MissionsService.UpdateMissionParams): Observable<StrictHttpResponse<Item>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    console.log(params);
    __body = params.body;
    __body.id = params.identifier;
    console.log(__body);
    let req = new HttpRequest<any>(
      'PUT',
      this.rootUrl + `/mission/${params.identifier}`,
       __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      }
    );
    console.log(req);
    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<Item>;
      })
    );
  }

  /**
   * Supplying a mission, this will update the Mission identified by the identifier in the path with contents from the passed in request body.
   * @param params The `MissionsService.UpdateMissionParams` containing the following parameters:
   *
   * - `identifier`:
   *
   * - `body`:
   *
   * @return successful operation
   */
  updateMission(params: MissionsService.UpdateMissionParams): Observable<Item> {
    console.log(params);
    return this.updateMissionResponse(params).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Deletes a specific mission known to the server.
   * @param nameOrId undefined
   */
  deleteMissionResponse(nameOrId: string): Observable<StrictHttpResponse<void>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    let req = new HttpRequest<any>(
      "DELETE",
      this.rootUrl + `/mission/${nameOrId}`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'text'
      }
    );

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r.clone({ body: null}) as StrictHttpResponse<void>
      })
    );
  }

  /**
   * Deletes a specific mission known to the server.
   * @param nameOrId undefined
   */
  deleteMission(nameOrId: string): Observable<void> {
    return this.deleteMissionResponse(nameOrId).pipe(
      __map(_r => _r.body)
    );
  }
}

module MissionsService {

  /**
   * Parameters for getMissions
   */
  export interface GetMissionsParams {
    tags?: Array<string>;
    search?: string;
    offset?: number;
    count?: number;
  }

  /**
   * Parameters for updateMission
   */
  export interface UpdateMissionParams {
    identifier: string;
    body?: Mission;
  }
}
