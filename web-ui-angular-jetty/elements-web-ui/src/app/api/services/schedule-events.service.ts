/* tslint:disable */
import {Injectable} from '@angular/core';
import {ApiConfiguration} from '../api-configuration';
import {HttpClient, HttpHeaders, HttpRequest, HttpResponse} from '@angular/common/http';
import {BaseService} from '../base-service';
import {Observable} from 'rxjs';
import {StrictHttpResponse} from '../strict-http-response';
import {filter as __filter, map as __map} from 'rxjs/operators';
import {CreateScheduleEventRequest, ScheduleEvent, UpdateScheduleEventRequest} from '../models';
import {Pagination} from "../models/pagination";

@Injectable({
  providedIn: 'root'
})
export class ScheduleEventsService extends BaseService {

  constructor(
    config: ApiConfiguration,
    http: HttpClient
  ) {
    super(config, http);
  }

  /**
   * Searches all ScheduleEvents and returns all matching schedules, filtered by the passed in search parameters.
   * If multiple tags are specified,then all items that contain an least one of the passed in tags are returned.
   * @param params The 'ScheduleEventsService.GetScheduleEventsParams' containing the following parameters:
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
  getScheduleEventsResponse(params: ScheduleEventsService.GetScheduleEventsParams) : Observable<StrictHttpResponse<Pagination<ScheduleEvent>>> {

    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;
    (params.tags || []).forEach((val, index) => {if (val != null) __params = __params.append('tags', val.toString())});
    if (params.search != null) __params = __params.set('search', params.search.toString());
    if (params.offset != null) __params = __params.set('offset', params.offset.toString());
    if (params.count != null) __params = __params.set('count', params.count.toString());
    let req = new HttpRequest<any>(
      'GET',
      // this.rootUrl + '/schedule',
      this.rootUrl + '/schedule/' + params.scheduleNameOrId + '/event',
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<Pagination<ScheduleEvent>>;
      })
    );

  }

  /**
   * Searches all schedules and returns all matching schedules, filtered by the passed in search parameters.
   * If multiple tags are specified, then all schedules that contain at least one of the passed in tags is returned.
   * @param params The `ScheduleEventsService.GetScheduleEventsParams` containing the following parameters:
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
  getScheduleEvents(params: ScheduleEventsService.GetScheduleEventsParams): Observable<Pagination<ScheduleEvent>> {
    return this.getScheduleEventsResponse(params).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Supplying a scheduleEvent object, this will create a new scheduleEvent with a newly assigned unique id.
   * @param body CreateScheduleEventRequest
   * @param scheduleId
   * @return successful operation
   */
  createScheduleEventResponse(body: CreateScheduleEventRequest, scheduleId: string): Observable<StrictHttpResponse<ScheduleEvent>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;
    __body = body;

    let req = new HttpRequest<any>(
      'POST',
      this.rootUrl + '/schedule/' + scheduleId + '/event',
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
        return _r as StrictHttpResponse<ScheduleEvent>;
      })
    );
  }

  /**
   * Supplying a scheduleEvent object, this will create a new scheduleEvent with a newly assigned unique id.
   * @param body CreateScheduleEventRequest
   * @param scheduleId
   * @return successful operation
   */
  createScheduleEvent(body: CreateScheduleEventRequest, scheduleId: string): Observable<ScheduleEvent> {
    return this.createScheduleEventResponse(body, scheduleId).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Supplying a scheduleEvent, this will update the ScheduleEvent identified by the identifier in the path with contents from the passed in request body.
   * @return successful operation
   * @param body
   * @param scheduleId
   * @param eventId
   */
  updateScheduleEventResponse(body: UpdateScheduleEventRequest, scheduleId: string, eventId: string): Observable<StrictHttpResponse<ScheduleEvent>> {
    let __headers = new HttpHeaders();
    let __body: any = null;

    __body = body;
    let req = new HttpRequest<any>(
      'PUT',
      this.rootUrl + '/schedule/' + scheduleId + '/event/' + eventId,
       __body,
      {
        headers: __headers,
        responseType: 'json'
      }
    );
    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<ScheduleEvent>;
      })
    );
  }

  /**
   * Supplying a scheduleEvent, this will update the Schedule event identified by the identifier in the path with contents from the passed in request body.
   * @return successful operation
   * @param body
   * @param shceduleId
   * @param eventId
   */
  updateScheduleEvent(body: UpdateScheduleEventRequest, shceduleId: string, eventId: string): Observable<ScheduleEvent> {
    return this.updateScheduleEventResponse(body, shceduleId, eventId).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Deletes a specific schedule known to the server.
   * @param nameOrId undefined
   */
  deleteScheduleEventResponse(scheduleId: string, nameOrId: string): Observable<StrictHttpResponse<void>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    let req = new HttpRequest<any>(
      "DELETE",
      this.rootUrl + '/schedule/' + scheduleId + '/event/' + nameOrId,
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
  deleteScheduleEvent(scheduleId: string, nameOrId: string): Observable<void> {
    return this.deleteScheduleEventResponse(scheduleId, nameOrId).pipe(
      __map(_r => _r.body)
    );
  }
}

module ScheduleEventsService {

  /**
   * Parameters to fetch a specific schedule event.
   */
  export interface GetScheduleEventParams {
    scheduleNameOrId: string;
    scheduleEventId: string;
  }

  /**
   * Parameters for getScheduleEvents
   */
  export interface GetScheduleEventsParams {
    scheduleNameOrId: string;
    tags?: Array<string>;
    search?: string;
    offset?: number;
    count?: number;
  }

}
