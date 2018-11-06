/* tslint:disable */
import { Injectable } from '@angular/core';
import { HttpClient, HttpRequest, HttpResponse, HttpHeaders, HttpParams } from '@angular/common/http';
import { BaseService } from '../base-service';
import { ApiConfiguration } from '../api-configuration';
import { StrictHttpResponse } from '../strict-http-response';
import { Observable } from 'rxjs';
import { map as __map, filter as __filter } from 'rxjs/operators';

import { SessionCreation } from '../models/session-creation';
import { UsernamePasswordSessionRequest } from '../models/username-password-session-request';
@Injectable({
  providedIn: 'root',
})
class UsernamePasswordSessionService extends BaseService {
  constructor(
    config: ApiConfiguration,
    http: HttpClient
  ) {
    super(config, http);
  }

  /**
   * Begins a session by accepting both the UserID and the Passoword.  Upon successful completion of this call, the user will be added to the current HTTP session.  If the session expires, the user will have to reestablish the session by supplying credentials again.  This is most useful for applications delivered in a web page.
   * @param body undefined
   * @return successful operation
   */
  createSessionResponse(body?: UsernamePasswordSessionRequest): Observable<StrictHttpResponse<SessionCreation>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;
    __body = body;
    let req = new HttpRequest<any>(
      'POST',
      this.rootUrl + `/session`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<SessionCreation>;
      })
    );
  }

  /**
   * Begins a session by accepting both the UserID and the Passoword.  Upon successful completion of this call, the user will be added to the current HTTP session.  If the session expires, the user will have to reestablish the session by supplying credentials again.  This is most useful for applications delivered in a web page.
   * @param body undefined
   * @return successful operation
   */
  createSession(body?: UsernamePasswordSessionRequest): Observable<SessionCreation> {
    return this.createSessionResponse(body).pipe(
      __map(_r => _r.body)
    );
  }
}

module UsernamePasswordSessionService {
}

export { UsernamePasswordSessionService }
