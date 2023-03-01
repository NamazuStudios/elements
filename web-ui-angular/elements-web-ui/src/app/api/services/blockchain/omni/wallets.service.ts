import { HttpClient, HttpHeaders, HttpRequest, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map as __map, filter as __filter } from 'rxjs/operators';
import { ApiConfiguration } from 'src/app/api/api-configuration';
import { BaseService } from 'src/app/api/base-service';
import { CreateWalletRequest, UpdateWalletRequest, Wallet } from 'src/app/api/models/omni/wallets';
import { Pagination } from 'src/app/api/models/pagination';
import { StrictHttpResponse } from 'src/app/api/strict-http-response';

@Injectable({
  providedIn: 'root'
})
class WalletsService extends BaseService {
  constructor(
    config: ApiConfiguration,
    http: HttpClient
  ) {
    super(config, http);
  }

  getWalletsResponse(params: WalletsService.GetWalletsParams): Observable<StrictHttpResponse<Pagination<Wallet>>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;
    if (params.offset != null) __params = __params.set('offset', params.offset.toString());
    if (params.count != null) __params = __params.set('count', params.count.toString());
    if (params.userId != null) __params = __params.set('userId', params.userId.toString());
    let req = new HttpRequest<any>(
      'GET',
       `${this.rootUrl}/blockchain/omni/wallet`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<Pagination<Wallet>>;
      }),
    );
  }

  getWallets(params: WalletsService.GetWalletsParams) {
    return this.getWalletsResponse(params).pipe(
      __map(_r => _r.body)
    );
  }

  createWalletResponse(params: WalletsService.CreateWalletParams): Observable<StrictHttpResponse<Wallet>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body = params.body;
    console.log('===>', __body);
    let req = new HttpRequest<any>(
      'POST',
       `${this.rootUrl}/blockchain/omni/vault/${params.vaultId}/wallet`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json',
      });
    console.log('===>', req);

    return this.http.request<any>(req).pipe(
      __filter((_r) => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        console.log('===>', _r);
        return _r as StrictHttpResponse<Wallet>;
      })
    );
  }

  createWallet(params: WalletsService.CreateWalletParams): Observable<Wallet> {
    return this.createWalletResponse(params).pipe(
      __map(_r => _r.body),
    );
  }

  editWalletResponse(params: WalletsService.UpdateWalletParams): Observable<StrictHttpResponse<Wallet>> {
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
        return _r as StrictHttpResponse<Wallet>;
      })
    );
  }

  editWallet(params: WalletsService.UpdateWalletParams) {
    return this.editWalletResponse(params).pipe(
      __map(_r => _r.body)
    );
  }

  getWalletsByVaultResponse(params: WalletsService.GetWalletsByVaultParams): Observable<StrictHttpResponse<Pagination<Wallet>>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body = null;
    if (params.offset != null) __params = __params.set('offset', params.offset.toString());
    if (params.count != null) __params = __params.set('count', params.count.toString());
    if (params.userId != null) __params = __params.set('userId', params.api.toString());

    let req = new HttpRequest<any>(
      'GET',
      params.vaultId
        ? `${this.rootUrl}/blockchain/omni/vault/${params.vaultId}/wallet`
        : `${this.rootUrl}/blockchain/omni/wallet`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter((_r) => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<Pagination<Wallet>>;
      })
    );
  }

  getWalletsByVault(params: WalletsService.GetWalletsByVaultParams) {
    return this.getWalletsByVaultResponse(params).pipe(
      __map(_r => _r.body)
    );
  }

  deleteWalletResponse(vaultId: string, walletId: string) {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body = null;
    let req = new HttpRequest<any>(
      'DELETE',
       `${this.rootUrl}/blockchain/omni/vault/${vaultId}/wallet/${walletId}`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter((_r) => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r;
      })
    );
  }

  deleteWallet(vaultId: string, walletId: string) {
    return this.deleteWalletResponse(vaultId, walletId).pipe(
      __map(_r => _r.body)
    );
  }
}

module WalletsService {
  /**
   * Parameters for getWallets
   */
  export interface GetWalletsParams {
    offset?: number;
    count?: number;
    userId?: string;
    format?: string;
  }

  export interface CreateWalletParams {
    vaultId: string;
    body: CreateWalletRequest;
  }

  export interface UpdateWalletParams {
    id: string;
    body: UpdateWalletRequest;
  }

  export interface GetWalletsByVaultParams {
    offset?: number;
    count?: number;
    userId?: string;
    api?: string;
    network?: string[];
    vaultId?: string;
  }
}

export { WalletsService };
