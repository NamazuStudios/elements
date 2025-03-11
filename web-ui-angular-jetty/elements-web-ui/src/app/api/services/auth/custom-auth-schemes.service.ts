/* tslint:disable */
import { Injectable } from "@angular/core";
import {
  HttpClient,
  HttpRequest,
  HttpResponse,
  HttpHeaders,
} from "@angular/common/http";
import { BaseService } from "../../base-service";
import { ApiConfiguration } from "../../api-configuration";
import { StrictHttpResponse } from "../../strict-http-response";
import { Observable } from "rxjs";
import { map as __map, filter as __filter } from "rxjs/operators";
import { CustomAuthScheme } from "../../models/auth/auth-scheme-custom";
import { UpdateCustomAuthSchemeRequest } from "../../models/auth/update-custom-auth-scheme-request";
import { PaginationCustomAuthScheme } from "../../models/auth/pagination-custom-auth-scheme";
import { CreateCustomAuthSchemeRequest } from "../../models/auth/create-custom-auth-scheme-request";

@Injectable({
  providedIn: "root",
})
class CustomAuthSchemesService extends BaseService {
  constructor(config: ApiConfiguration, http: HttpClient) {
    super(config, http);
  }

  /**
   * Gets a pagination of Auth Schemes for the given query.
   * @param params The `AuthSchemesService.GetAuthSchemesParams` containing the following parameters:
   *
   * - `tags`:
   *
   * - `query`:
   *
   * - `offset`:
   *
   * - `count`:
   *
   * @return successful operation
   */
  getAuthSchemesResponse(
    params: CustomAuthSchemesService.GetCustomAuthSchemesParams
  ): Observable<StrictHttpResponse<PaginationCustomAuthScheme>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;
    (params.tags || []).forEach((val, index) => {
      if (val != null) __params = __params.append("tags", val.toString());
    });
    if (params.query != null)
      __params = __params.set("search", params.query.toString());
    if (params.offset != null)
      __params = __params.set("offset", params.offset.toString());
    if (params.count != null)
      __params = __params.set("count", params.count.toString());
    let req = new HttpRequest<any>(
      "GET",
      this.rootUrl + `/auth_scheme`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: "json",
      }
    );

    return this.http.request<any>(req).pipe(
      __filter((_r) => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<PaginationCustomAuthScheme>;
      })
    );
  }

  /**
   * Gets a pagination of Auth Schemes for the given query.
   * @param params The `AuthSchemesService.GetAuthSchemesParams` containing the following parameters:
   *
   * - `tags`:
   *
   * - `query`:
   *
   * - `offset`:
   *
   * - `count`:
   *
   * @return successful operation
   */
  getAuthSchemes(
    params: CustomAuthSchemesService.GetCustomAuthSchemesParams
  ): Observable<PaginationCustomAuthScheme> {
    return this.getAuthSchemesResponse(params).pipe(__map((_r) => _r.body));
  }

  /**
   * Updates a Auth Schemes with the specified AuthScheme id.
   * @param params The `AuthSchemesService.UpdateAuthSchemeParams` containing the following parameters:
   *
   * - `id`:
   *
   * - `body`:
   *
   * @return successful operation
   */
  updateAuthSchemeResponse(
    params: CustomAuthSchemesService.UpdateCustomAuthSchemeParams
  ): Observable<StrictHttpResponse<CustomAuthScheme>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    __body = params.body;
    let req = new HttpRequest<any>(
      "PUT",
      this.rootUrl + `/auth_scheme/${params.id}`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: "json",
      }
    );

    return this.http.request<any>(req).pipe(
      __filter((_r) => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<CustomAuthScheme>;
      })
    );
  }

  /**
   * Updates a Auth Scheme with the specified AuthScheme id.
   * @param params The `AuthSchemesService.UpdateAuthSchemeParams` containing the following parameters:
   *
   * - `id`:
   *
   * - `body`:
   *
   * @return successful operation
   */
  updateAuthScheme(
    params: CustomAuthSchemesService.UpdateCustomAuthSchemeParams
  ): Observable<CustomAuthScheme> {
    return this.updateAuthSchemeResponse(params).pipe(__map((_r) => _r.body));
  }

  /**
   * Deletes a Auth Scheme with the specified id.
   * @param authSchemeId undefined
   */
  deleteAuthSchemeResponse(
    authSchemeId: string
  ): Observable<StrictHttpResponse<void>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    let req = new HttpRequest<any>(
      "DELETE",
      this.rootUrl + `/auth_scheme/${authSchemeId}`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: "text",
      }
    );

    return this.http.request<any>(req).pipe(
      __filter((_r) => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r.clone({ body: null }) as StrictHttpResponse<void>;
      })
    );
  }

  /**
   * Deletes a Auth Scheme with the specified id.
   * @param authSchemeId undefined
   */
  deleteAuthScheme(authSchemeId: string): Observable<void> {
    return this.deleteAuthSchemeResponse(authSchemeId).pipe(
      __map((_r) => _r.body)
    );
  }

  /**
   * Gets a specific AuthScheme by AuthScheme id.
   * @param authSchemeId undefined
   * @return successful operation
   */
  getAuthSchemeResponse(
    authSchemeId: string
  ): Observable<StrictHttpResponse<CustomAuthScheme>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    let req = new HttpRequest<any>(
      "GET",
      this.rootUrl + `/auth_scheme/${authSchemeId}`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: "json",
      }
    );

    return this.http.request<any>(req).pipe(
      __filter((_r) => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<CustomAuthScheme>;
      })
    );
  }

  /**
   * Gets a specific AuthScheme by Id.
   * @param authSchemeId undefined
   * @return successful operation
   */
  getAuthScheme(authSchemeId: string): Observable<CustomAuthScheme> {
    return this.getAuthSchemeResponse(authSchemeId).pipe(
      __map((_r) => _r.body)
    );
  }

  /**
   * Creates a new AuthScheme definition.
   * @param body undefined
   * @return successful operation
   */
  createAuthSchemeResponse(
    body?: CreateCustomAuthSchemeRequest
  ): Observable<StrictHttpResponse<CustomAuthScheme>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;
    __body = body;
    let req = new HttpRequest<any>(
      "POST",
      this.rootUrl + `/auth_scheme`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: "json",
      }
    );

    return this.http.request<any>(req).pipe(
      __filter((_r) => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<CustomAuthScheme>;
      })
    );
  }

  /**
   * Creates a new AuthScheme definition.
   * @param body undefined
   * @return successful operation
   */
  createAuthScheme(body?: CreateCustomAuthSchemeRequest): Observable<CustomAuthScheme> {
    return this.createAuthSchemeResponse(body).pipe(__map((_r) => _r.body));
  }
}

module CustomAuthSchemesService {
  /**
   * Parameters for updateAuthScheme
   */
  export interface UpdateCustomAuthSchemeParams {
    id: string;
    body?: UpdateCustomAuthSchemeRequest;
  }

  /**
   * Parameters for getAuthSchemes
   */
  export interface GetCustomAuthSchemesParams {
    offset?: number;
    count?: number;
    tags?: Array<string>;
    query?: string;
  }
}

export { CustomAuthSchemesService };
