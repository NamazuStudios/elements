/* tslint:disable */
import { Injectable } from '@angular/core';
import { HttpClient, HttpRequest, HttpResponse, HttpHeaders, HttpParams } from '@angular/common/http';
import { BaseService } from '../base-service';
import { ApiConfiguration } from '../api-configuration';
import { StrictHttpResponse } from '../strict-http-response';
import { Observable } from 'rxjs';
import { map as __map, filter as __filter } from 'rxjs/operators';

import { MockSessionCreation } from '../models/mock-session-creation';
import { MockSessionRequest } from '../models/mock-session-request';
@Injectable({
  providedIn: 'root',
})
class MockSessionsService extends BaseService {
  constructor(
    config: ApiConfiguration,
    http: HttpClient
  ) {
    super(config, http);
  }

  /**
   * Begins a session by accepting a mock session request.  The request must be made with an authenticated super-user.
   * @param body undefined
   * @return successful operation
   */
  createSessionResponse(body?: MockSessionRequest): Observable<StrictHttpResponse<MockSessionCreation>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;
    __body = body;
    let req = new HttpRequest<any>(
      'POST',
      this.rootUrl + `/mock_session`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<MockSessionCreation>;
      })
    );
  }

  /**
   * Begins a session by accepting a mock session request.  The request must be made with an authenticated super-user.
   * @param body undefined
   * @return successful operation
   */
  createSession(body?: MockSessionRequest): Observable<MockSessionCreation> {
    return this.createSessionResponse(body).pipe(
      __map(_r => _r.body)
    );
  }
}

module MockSessionsService {
}

export { MockSessionsService }
