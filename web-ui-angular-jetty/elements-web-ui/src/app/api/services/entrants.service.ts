/* tslint:disable */
import { Injectable } from '@angular/core';
import { HttpClient, HttpRequest, HttpResponse, HttpHeaders, HttpParams } from '@angular/common/http';
import { BaseService } from '../base-service';
import { ApiConfiguration } from '../api-configuration';
import { StrictHttpResponse } from '../strict-http-response';
import { Observable } from 'rxjs';
import { map as __map, filter as __filter } from 'rxjs/operators';

import { SocialCampaignEntry } from '../models/social-campaign-entry';
import { BasicEntrantProfile } from '../models/basic-entrant-profile';
import { SteamEntrantProfile } from '../models/steam-entrant-profile';
@Injectable({
  providedIn: 'root',
})
class EntrantsService extends BaseService {
  constructor(
    config: ApiConfiguration,
    http: HttpClient
  ) {
    super(config, http);
  }

  /**
   * A basic entrant is keyed uniquely from email and provides very simple contact information.  The entrant can be associated with a User later if necessary.
   * @param params The `EntrantsService.AddBasicEntrantParams` containing the following parameters:
   *
   * - `name`:
   *
   * - `body`:
   *
   * @return successful operation
   */
  addBasicEntrantResponse(params: EntrantsService.AddBasicEntrantParams): Observable<StrictHttpResponse<SocialCampaignEntry>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    __body = params.body;
    let req = new HttpRequest<any>(
      'POST',
      this.rootUrl + `/campaign/${params.name}/basic/entrant`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<SocialCampaignEntry>;
      })
    );
  }

  /**
   * A basic entrant is keyed uniquely from email and provides very simple contact information.  The entrant can be associated with a User later if necessary.
   * @param params The `EntrantsService.AddBasicEntrantParams` containing the following parameters:
   *
   * - `name`:
   *
   * - `body`:
   *
   * @return successful operation
   */
  addBasicEntrant(params: EntrantsService.AddBasicEntrantParams): Observable<SocialCampaignEntry> {
    return this.addBasicEntrantResponse(params).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * A Steam entrant is simalar to a basic entrant, but captures the user's Steam ID in addition to the remaining basic info.
   * @param params The `EntrantsService.AddSteamEntrantParams` containing the following parameters:
   *
   * - `name`:
   *
   * - `body`:
   *
   * @return successful operation
   */
  addSteamEntrantResponse(params: EntrantsService.AddSteamEntrantParams): Observable<StrictHttpResponse<SocialCampaignEntry>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    __body = params.body;
    let req = new HttpRequest<any>(
      'POST',
      this.rootUrl + `/campaign/${params.name}/steam/entrant`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<SocialCampaignEntry>;
      })
    );
  }

  /**
   * A Steam entrant is simalar to a basic entrant, but captures the user's Steam ID in addition to the remaining basic info.
   * @param params The `EntrantsService.AddSteamEntrantParams` containing the following parameters:
   *
   * - `name`:
   *
   * - `body`:
   *
   * @return successful operation
   */
  addSteamEntrant(params: EntrantsService.AddSteamEntrantParams): Observable<SocialCampaignEntry> {
    return this.addSteamEntrantResponse(params).pipe(
      __map(_r => _r.body)
    );
  }
}

module EntrantsService {

  /**
   * Parameters for addBasicEntrant
   */
  export interface AddBasicEntrantParams {
    name: string;
    body?: BasicEntrantProfile;
  }

  /**
   * Parameters for addSteamEntrant
   */
  export interface AddSteamEntrantParams {
    name: string;
    body?: SteamEntrantProfile;
  }
}

export { EntrantsService }
