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
import { OidcAuthScheme } from "../../models/auth/auth-scheme-oidc";
import { UpdateOidcAuthSchemeRequest } from "../../models/auth/update-oidc-auth-scheme-request";
import { PaginationOidcAuthScheme } from "../../models/auth/pagination-oidc-auth-scheme";
import { CreateOidcAuthSchemeRequest } from "../../models/auth/create-oidc-auth-scheme-request";

@Injectable({
  providedIn: "root",
})
class OidcAuthSchemesService extends BaseService {
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
    params: OidcAuthSchemesService.GetAuthSchemesParams
  ): Observable<StrictHttpResponse<PaginationOidcAuthScheme>> {
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
      this.rootUrl + `/auth_scheme/oidc`,
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
        return _r as StrictHttpResponse<PaginationOidcAuthScheme>;
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
    params: OidcAuthSchemesService.GetAuthSchemesParams
  ): Observable<PaginationOidcAuthScheme> {
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
    params: OidcAuthSchemesService.UpdateAuthSchemeParams
  ): Observable<StrictHttpResponse<OidcAuthScheme>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    __body = params.body;
    let req = new HttpRequest<any>(
      "PUT",
      this.rootUrl + `/auth_scheme/oidc/${params.id}`,
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
        return _r as StrictHttpResponse<OidcAuthScheme>;
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
    params: OidcAuthSchemesService.UpdateAuthSchemeParams
  ): Observable<OidcAuthScheme> {
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
      this.rootUrl + `/auth_scheme/oidc/${authSchemeId}`,
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
  ): Observable<StrictHttpResponse<OidcAuthScheme>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    let req = new HttpRequest<any>(
      "GET",
      this.rootUrl + `/auth_scheme/oidc/${authSchemeId}`,
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
        return _r as StrictHttpResponse<OidcAuthScheme>;
      })
    );
  }

  /**
   * Gets a specific AuthScheme by Id.
   * @param authSchemeId undefined
   * @return successful operation
   */
  getAuthScheme(authSchemeId: string): Observable<OidcAuthScheme> {
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
    body?: CreateOidcAuthSchemeRequest
  ): Observable<StrictHttpResponse<OidcAuthScheme>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;
    __body = body;
    let req = new HttpRequest<any>(
      "POST",
      this.rootUrl + `/auth_scheme/oidc`,
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
        return _r as StrictHttpResponse<OidcAuthScheme>;
      })
    );
  }

  /**
   * Creates a new AuthScheme definition.
   * @param body undefined
   * @return successful operation
   */
  createAuthScheme(body?: CreateOidcAuthSchemeRequest): Observable<OidcAuthScheme> {
    return this.createAuthSchemeResponse(body).pipe(__map((_r) => _r.body));
  }
}

module OidcAuthSchemesService {
  /**
   * Parameters for updateAuthScheme
   */
  export interface UpdateAuthSchemeParams {
    id: string;
    body?: UpdateOidcAuthSchemeRequest;
  }

  /**
   * Parameters for getAuthSchemes
   */
  export interface GetAuthSchemesParams {
    offset?: number;
    count?: number;
    tags?: Array<string>;
    query?: string;
  }
}

export { OidcAuthSchemesService };
