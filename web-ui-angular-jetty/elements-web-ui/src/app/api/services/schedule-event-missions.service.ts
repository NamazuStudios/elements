/* tslint:disable */
import {Injectable} from '@angular/core';
import {ApiConfiguration} from '../api-configuration';
import {HttpClient, HttpHeaders, HttpRequest, HttpResponse} from '@angular/common/http';
import {BaseService} from '../base-service';
import {Observable} from 'rxjs';
import {StrictHttpResponse} from '../strict-http-response';
import {filter as __filter, map as __map} from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class ScheduleEventMissionsService extends BaseService {

  constructor(
    config: ApiConfiguration,
    http: HttpClient
  ) {
    super(config, http);
  }

  // getScheduleEventMissionsResponse(params: ScheduleEventMissionsService.GetScheduleEventMissionsParams) : Observable<StrictHttpResponse<Pagination<Mission>>> {
  //
  //   let __params = this.newParams();
  //   let __headers = new HttpHeaders();
  //   let __body: any = null;
  //   (params.tags || []).forEach((val, index) => {if (val != null) __params = __params.append('tags', val.toString())});
  //   if (params.search != null) __params = __params.set('search', params.search.toString());
  //   if (params.offset != null) __params = __params.set('offset', params.offset.toString());
  //   if (params.count != null) __params = __params.set('count', params.count.toString());
  //   let req = new HttpRequest<any>(
  //     'GET',
  //     // this.rootUrl + '/schedule',
  //     this.rootUrl + '/schedule/' + params.scheduleNameOrId + '/event',
  //     __body,
  //     {
  //       headers: __headers,
  //       params: __params,
  //       responseType: 'json'
  //     });
  //
  //   return this.http.request<any>(req).pipe(
  //     __filter(_r => _r instanceof HttpResponse),
  //     __map((_r: HttpResponse<any>) => {
  //       return _r as StrictHttpResponse<Pagination<ScheduleEvent>>;
  //     })
  //   );
  //
  // }

  // getScheduleEventMissions(params: ScheduleEventMissionsService.GetScheduleEventMissionsParams): Observable<Pagination<Mission>> {
    // return this.getScheduleEventMissionsResponse(params).pipe(
    //   __map(_r => _r.body)
    // );
  // }

  deleteScheduleEventMissionResponse(scheduleId: string, eventId: string, nameOrId: string): Observable<StrictHttpResponse<void>> {
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

  deleteScheduleEventMission(scheduleId: string, eventId: string, nameOrId: string): Observable<void> {
    return this.deleteScheduleEventMissionResponse(scheduleId, eventId, nameOrId).pipe(
      __map(_r => _r.body)
    );
  }
}

module ScheduleEventMissionsService {

  export interface GetScheduleEventMissionsParams {
    id: string;
    tags?: Array<string>;
    search?: string;
    offset?: number;
    count?: number;
  }

}
