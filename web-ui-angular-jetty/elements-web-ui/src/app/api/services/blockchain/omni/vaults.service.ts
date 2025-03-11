import { HttpClient, HttpHeaders, HttpRequest, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map as __map, filter as __filter } from 'rxjs/operators';
import { ApiConfiguration } from '../../../api-configuration';
import { BaseService } from '../../../base-service';
import { CreateVaultParams, UpdateVaultRequest, Vault } from '../../../models/omni/vaults';
import { Pagination } from '../../../models/pagination';
import { StrictHttpResponse } from '../../../strict-http-response';

@Injectable({
  providedIn: 'root'
})
class VaultsService extends BaseService {
  constructor(
    config: ApiConfiguration,
    http: HttpClient
  ) {
    super(config, http);
  }

  getVaultsResponse(params: VaultsService.GetVaultsParams): Observable<StrictHttpResponse<Pagination<Vault>>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;
    if (params.offset != null) __params = __params.set('offset', params.offset.toString());
    if (params.count != null) __params = __params.set('count', params.count.toString());
    if (params.userId != null) __params = __params.set('userId', params.userId.toString());
    let req = new HttpRequest<any>(
      'GET',
       `${this.rootUrl}/blockchain/omni/vault`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<Pagination<Vault>>;
      })
    );
  }

  getVaults(params: VaultsService.GetVaultsParams): Observable<Pagination<Vault>> {
    return this.getVaultsResponse(params).pipe(
      __map(_r => _r.body)
    );
  }

  createVaultResponse(body: CreateVaultParams): Observable<StrictHttpResponse<Vault>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body = body;
    let req = new HttpRequest<any>(
      'POST',
       `${this.rootUrl}/blockchain/omni/vault`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter((_r) => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<Vault>;
      })
    );
  }

  createVault(body: CreateVaultParams): Observable<Vault> {
    return this.createVaultResponse(body).pipe(
      __map(_r => _r.body)
    );
  }

  editVaultResponse(params: VaultsService.UpdateVaultParams): Observable<StrictHttpResponse<Vault>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body = params.body;
    let req = new HttpRequest<any>(
      'PUT',
       `${this.rootUrl}/blockchain/omni/vault/${params.id}`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter((_r) => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<Vault>;
      })
    );
  }

  editVault(params: VaultsService.UpdateVaultParams) {
    return this.editVaultResponse(params).pipe(
      __map(_r => _r.body)
    );
  }

  deleteVaultResponse(vaultId: string) {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body = null;
    let req = new HttpRequest<any>(
      'DELETE',
       `${this.rootUrl}/blockchain/omni/vault/${vaultId}`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter((_r) => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<Vault>;
      })
    );
  }

  deleteVault(vaultId: string) {
    return this.deleteVaultResponse(vaultId).pipe(
      __map(_r => _r.body)
    );
  }
}

module VaultsService {
  export interface GetVaultsParams {
    offset?: number;
    count?: number;
    userId?: string;
  }

  export interface UpdateVaultParams {
    id: string;
    body: UpdateVaultRequest;
  }
}

export { VaultsService };
