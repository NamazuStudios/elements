/* tslint:disable */
import { Injectable } from '@angular/core';
import { HttpClient, HttpRequest, HttpResponse, HttpHeaders} from '@angular/common/http';
import { BaseService } from '../base-service';
import { ApiConfiguration } from '../api-configuration';
import { StrictHttpResponse } from '../strict-http-response';
import { Observable } from 'rxjs';
import { map as __map, filter as __filter } from 'rxjs/operators';

import { NeoWallet } from '../models/wallet/neo-wallet';
import { PaginationNeoWallet } from '../models/wallet/pagination-neo-wallet';
@Injectable({
  providedIn: 'root',
})
class NeoWalletsService extends BaseService {
  constructor(
    config: ApiConfiguration,
    http: HttpClient
  ) {
    super(config, http);
  }

  /**
   * Gets the metadata for a single wallet.  This may include more specific details not availble in the bulk-get or fetch operation.
   * @param walletId undefined
   * @return successful operation
   */
  getWalletResponse(walletId: string): Observable<StrictHttpResponse<NeoWallet>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    let req = new HttpRequest<any>(
      'GET',
      this.rootUrl + `/blockchain/neo/wallet/${walletId}`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<NeoWallet>;
      })
    );
  }

  /**
   * Gets the metadata for a single wallet.  This may include more specific details not availble in the bulk-get or fetch operation.
   * @param walletId undefined
   * @return successful operation
   */
  getWallet(walletId: string): Observable<NeoWallet> {
    return this.getWalletResponse(walletId).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Updates a Neo Wallet with the specified walletId.
   * @param params The `WalletsService.UpdateWalletParams` containing the following parameters:
   *
   * - `id`:
   *
   * - `body`:
   *
   * @return successful operation
   */
  updateWalletResponse(params: NeoWalletsService.UpdateWalletParams): Observable<StrictHttpResponse<NeoWallet>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    __body = params.body;
    let req = new HttpRequest<any>(
      'PUT',
      this.rootUrl + `/blockchain/neo/wallet/${params.id}`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<NeoWallet>;
      })
    );
  }

  /**
   * Updates a Neo Wallet with the specified walletId.
   * @param params The `WalletsService.UpdateWalletParams` containing the following parameters:
   *
   * - `walletId`:
   *
   * - `body`:
   *
   * @return successful operation
   */
  updateWallet(params: NeoWalletsService.UpdateWalletParams): Observable<NeoWallet> {
    return this.updateWalletResponse(params).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Deletes a Neo Wallet with the specified id.
   * @param walletId undefined
   */
  deleteWalletResponse(walletId: string): Observable<StrictHttpResponse<void>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    let req = new HttpRequest<any>(
      'DELETE',
      this.rootUrl + `/blockchain/neo/wallet/${walletId}`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'text'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r.clone({ body: null }) as StrictHttpResponse<void>
      })
    );
  }

  /**
   * Deletes a Neo Wallet with the specified id.
   * @param walletId undefined
   */
  deleteWallet(walletId: string): Observable<void> {
    return this.deleteWalletResponse(walletId).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Gets a pagination of Neo Wallets for the given user id.
   * @param params The `WalletsService.GetWalletsParams` containing the following parameters:
   *
   * - `offset`:
   *
   * - `count`:
   *
   * - `userId`:
   *
   * - `format`:
   *
   * @return successful operation
   */
  getWalletsResponse(params: NeoWalletsService.GetWalletsParams): Observable<StrictHttpResponse<PaginationNeoWallet>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;
    if (params.offset != null) __params = __params.set('offset', params.offset.toString());
    if (params.count != null) __params = __params.set('count', params.count.toString());
    if (params.userId != null) __params = __params.set('userId', params.userId.toString());
    if (params.format != null) __params = __params.set('format', params.format.toString());
    let req = new HttpRequest<any>(
      'GET',
      this.rootUrl + `/blockchain/neo/wallet`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<PaginationNeoWallet>;
      })
    );
  }

  /**
   * Gets a pagination of Neo Wallets for the given user id.
   * @param params The `WalletsService.GetWalletsParams` containing the following parameters:
   *
   * - `offset`:
   *
   * - `count`:
   *
   * - `userId`:
   *
   * - `format`:
   *
   * @return successful operation
   */
  getWallets(params: NeoWalletsService.GetWalletsParams): Observable<PaginationNeoWallet> {
    return this.getWalletsResponse(params).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Creates a new Neo Wallet, associated with the given user.
   * @param body undefined
   * @return successful operation
   */
  createWalletResponse(body?: NeoWallet): Observable<StrictHttpResponse<NeoWallet>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;
    __body = body;
    let req = new HttpRequest<any>(
      'POST',
      this.rootUrl + `/blockchain/neo/wallet`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<NeoWallet>;
      })
    );
  }

  /**
   * Creates a new Neo Wallet, associated with the given user.
   * @param body undefined
   * @return successful operation
   */
  createWallet(body?: NeoWallet): Observable<NeoWallet> {
    return this.createWalletResponse(body).pipe(
      __map(_r => _r.body)
    );
  }
}

module NeoWalletsService {

  /**
   * Parameters for updateWallet
   */
  export interface UpdateWalletParams {
    id: string;
    body?: NeoWallet;
  }

  /**
   * Parameters for getWallets
   */
  export interface GetWalletsParams {
    offset?: number;
    count?: number;
    userId: string;
    format?: string;
  }
}

export { NeoWalletsService }
