/* tslint:disable */
import { Injectable } from '@angular/core';
import { HttpClient, HttpRequest, HttpResponse, HttpHeaders, HttpParams } from '@angular/common/http';
import { BaseService } from '../base-service';
import { ApiConfiguration } from '../api-configuration';
import { StrictHttpResponse } from '../strict-http-response';
import { Observable } from 'rxjs';
import { map as __map, filter as __filter } from 'rxjs/operators';

import { Version } from '../models/version';
@Injectable({
  providedIn: 'root',
})
class SeverVersionMetadataService extends BaseService {
  constructor(
    config: ApiConfiguration,
    http: HttpClient
  ) {
    super(config, http);
  }

  /**
   * Returns information about the current server version.  This should alwasy return theversion metadata.  This information is only known in packaged releases.
   * @return successful operation
   */
  getVersionResponse(): Observable<StrictHttpResponse<Version>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;
    let req = new HttpRequest<any>(
      'GET',
      this.rootUrl + `/version`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<Version>;
      })
    );
  }

  /**
   * Returns information about the current server version.  This should alwasy return theversion metadata.  This information is only known in packaged releases.
   * @return successful operation
   */
  getVersion(): Observable<Version> {
    return this.getVersionResponse().pipe(
      __map(_r => _r.body)
    );
  }
}

module SeverVersionMetadataService {
}

export { SeverVersionMetadataService }
