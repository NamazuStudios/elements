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
import {CreateScheduleRequest, Schedule, UpdateScheduleRequest} from '../models';
import {Pagination} from "../models/pagination";

@Injectable({
  providedIn: 'root'
})
export class SchedulesService extends BaseService {

  constructor(
    config: ApiConfiguration,
    http: HttpClient
  ) {
    super(config, http);
  }

  /**
   * Searches all schedules and returns all matching schedules, filtered by the passed in search parameters.
   * If multiple tags are specified,then all items that contain an least one of the passed in tags are returned.
   * @param params The 'SchedulesServce.GetSchedulesParams' containing the following parameters:
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
  getSchedulesResponse(params: SchedulesService.GetSchedulesParams) : Observable<StrictHttpResponse<Pagination<Schedule>>> {

    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;
    (params.tags || []).forEach((val, index) => {if (val != null) __params = __params.append('tags', val.toString())});
    if (params.search != null) __params = __params.set('search', params.search.toString());
    if (params.offset != null) __params = __params.set('offset', params.offset.toString());
    if (params.count != null) __params = __params.set('count', params.count.toString());
    let req = new HttpRequest<any>(
      'GET',
      this.rootUrl + `/schedule`,
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
   * Searches all schedules and returns all matching schedules, filtered by the passed in search parameters.
   * If multiple tags are specified, then all schedules that contain at least one of the passed in tags is returned.
   * @param params The `SchedulesService.GetSchedulesParams` containing the following parameters:
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
  getSchedules(params: SchedulesService.GetSchedulesParams): Observable<Pagination<Schedule>> {
    return this.getSchedulesResponse(params).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Supplying a schedule object, this will create a new schedule with a newly assigned unique id.
   * The Schedule representation returned in the response body is a representation of the Schedule as persisted with a unique identifier
   * signed and with its fields properly normalized.  The supplied schedule object submitted with the request must have a name property
   * that is unique across all schedules.
   * @param body undefined
   * @return successful operation
   */
  createScheduleResponse(body?: CreateScheduleRequest): Observable<StrictHttpResponse<Schedule>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any;
    __body = body;

    let req = new HttpRequest<any>(
      'POST',
      this.rootUrl + `/schedule`,
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
        return _r as StrictHttpResponse<Schedule>;
      })
    );
  }

  /**
   * Supplying a schedule object, this will create a new schedule with a newly assigned unique id.
   * The Schedule representation returned in the response body is a representation of the Schedule as persisted with a unique identifier
   * signed and with its fields properly normalized.  The supplied schedule object submitted with the request must have a name property
   * that is unique across all schedules.
   * @param body undefined
   * @return successful operation
   */
  createSchedule(body?: CreateScheduleRequest): Observable<Schedule> {
    return this.createScheduleResponse(body).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Looks up a schedule by the passed in identifier
   * @param identifier undefined
   * @return successful operation
   */
  getScheduleByIdentifierResponse(identifier: string): Observable<StrictHttpResponse<Schedule>> {

    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    let req = new HttpRequest<any>(
      'GET',
      this.rootUrl + `/schedule/${identifier}`,
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
   * Looks up a schedule by the passed in identifier
   * @param identifier undefined
   * @return successful operation
   */
  getScheduleByIdentifier(identifier: string): Observable<Schedule> {
    return this.getScheduleByIdentifierResponse(identifier).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Supplying a schedule, this will update the Schedule identified by the identifier in the path with contents from the passed in request body.
   * @param params The `SchedulesService.UpdateScheduleParams` containing the following parameters:
   *
   * - `identifier`:
   *
   * - `body`:
   *
   * @return successful operation
   */
  updateScheduleResponse(params: SchedulesService.UpdateScheduleParams): Observable<StrictHttpResponse<Schedule>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any;

    __body = params.body;
    __body.id = params.identifier;
    let req = new HttpRequest<any>(
      'PUT',
      this.rootUrl + `/schedule/${params.identifier}`,
       __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      }
    );
    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<Item>;
      })
    );
  }

  /**
   * Supplying a schedule, this will update the Schedule identified by the identifier in the path with contents from the passed in request body.
   * @param params The `SchedulesService.UpdateScheduleParams` containing the following parameters:
   *
   * - `identifier`:
   *
   * - `body`:
   *
   * @return successful operation
   */
  updateSchedule(params: SchedulesService.UpdateScheduleParams): Observable<Schedule> {
    return this.updateScheduleResponse(params).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Deletes a specific schedule known to the server.
   * @param nameOrId undefined
   */
  deleteScheduleResponse(nameOrId: string): Observable<StrictHttpResponse<void>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    let req = new HttpRequest<any>(
      "DELETE",
      this.rootUrl + `/schedule/${nameOrId}`,
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
   * Deletes a specific schedule known to the server.
   * @param nameOrId undefined
   */
  deleteSchedule(nameOrId: string): Observable<void> {
    return this.deleteScheduleResponse(nameOrId).pipe(
      __map(_r => _r.body)
    );
  }
}

module SchedulesService {

  /**
   * Parameters for getSchedules
   */
  export interface GetSchedulesParams {
    tags?: Array<string>;
    search?: string;
    offset?: number;
    count?: number;
  }

  /**
   * Parameters for updateSchedule
   */
  export interface UpdateScheduleParams {
    identifier: string;
    body?: UpdateScheduleRequest;
  }

}
