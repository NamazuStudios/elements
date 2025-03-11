/* tslint:disable */
import { Injectable } from '@angular/core';
import { HttpClient, HttpRequest, HttpResponse, HttpHeaders, HttpParams } from '@angular/common/http';
import { BaseService } from '../base-service';
import { ApiConfiguration } from '../api-configuration';
import { StrictHttpResponse } from '../strict-http-response';
import { Observable } from 'rxjs';
import { map as __map, filter as __filter } from 'rxjs/operators';

import { FacebookSessionCreation } from '../models/facebook-session-creation';
import { FacebookSessionRequest } from '../models/facebook-session-request';
@Injectable({
  providedIn: 'root',
})
class FacebookSessionService extends BaseService {
  constructor(
    config: ApiConfiguration,
    http: HttpClient
  ) {
    super(config, http);
  }

  /**
   * Begins a session by accepting a Facebook OAuth token, SocialEngine Application ID, and the configuration ID for the application.  This will generate a Session instance and return the result to the client.
   * @param body undefined
   * @return successful operation
   */
  createSessionResponse(body?: FacebookSessionRequest): Observable<StrictHttpResponse<FacebookSessionCreation>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;
    __body = body;
    let req = new HttpRequest<any>(
      'POST',
      this.rootUrl + `/facebook_session`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<FacebookSessionCreation>;
      })
    );
  }

  /**
   * Begins a session by accepting a Facebook OAuth token, SocialEngine Application ID, and the configuration ID for the application.  This will generate a Session instance and return the result to the client.
   * @param body undefined
   * @return successful operation
   */
  createSession(body?: FacebookSessionRequest): Observable<FacebookSessionCreation> {
    return this.createSessionResponse(body).pipe(
      __map(_r => _r.body)
    );
  }
}

module FacebookSessionService {
}

export { FacebookSessionService }
