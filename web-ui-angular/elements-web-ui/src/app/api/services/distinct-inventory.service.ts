import {Injectable} from "@angular/core";
import {BaseService} from "../base-service";
import {ApiConfiguration} from "../api-configuration";
import {HttpClient, HttpHeaders, HttpRequest, HttpResponse} from "@angular/common/http";
import {Observable} from "rxjs";
import {StrictHttpResponse} from "../strict-http-response";
import {PaginationDistinctInventoryItem} from "../models/pagination-distinct-inventory-item";
import {filter as __filter} from "rxjs/operators";
import {map as __map} from "rxjs/internal/operators/map";
import {FungibleInventoryItem} from "../models/fungible-inventory-item";
import {DistinctInventoryItem} from "../models/distinct-inventory-item";
import {PaginationFungibleInventoryItem} from "../models/pagination-fungible-inventory-item";
import UpdateInventoryParams = DistinctInventoryService.UpdateInventoryParams;

@Injectable({
  providedIn: 'root'
})
export class DistinctInventoryService extends BaseService {

  constructor(
      config: ApiConfiguration,
      http: HttpClient) {
    super(config, http);
  }

  /**
   * Searches all inventory items in the system and returns the metadata for all matches against the given search filter.
   * @param params The `InventoryService.GetInventoryParams` containing the following parameters:
   *
   * - `search`:
   *
   * - `offset`:
   *
   * - `count`:
   *
   * - `userId`:
   *
   * @return successful operation
   */
  getInventory(params: DistinctInventoryService.GetInventoryParams): Observable<PaginationDistinctInventoryItem> {
    return this.getInventoryResponse(params).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Searches all inventory items in the system and returns the metadata for all matches against the given search filter.
   * @param params The 'FungibleInventoryService.GetInventoryParams' containing the following parameters:
   *
   * - 'offset':
   *
   * - 'count':
   *
   * - 'userId':
   *
   * - 'search':
   *
   * @return successful operation
   */
  getInventoryResponse(params: DistinctInventoryService.GetInventoryParams): Observable<StrictHttpResponse<PaginationDistinctInventoryItem>> {

    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    if (params.offset != null) __params = __params.set('offset', params.offset.toString());
    if (params.count != null) __params = __params.set('count', params.count.toString());
    if (params.userId != null) __params = __params.set('userId', params.userId.toString());
    if (params.profileId != null) __params = __params.set('profileId', params.profileId.toString());
    if (params.search != null) __params = __params.set('search', params.search.toString());

    let req = new HttpRequest<any>(
      'GET',
      this.rootUrl + `/inventory/distinct`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<PaginationDistinctInventoryItem>;
      })
    );

  }

  createInventoryItem(body: { itemId: string; userId: string; profileId: string, metadata: any }) : Observable<StrictHttpResponse<DistinctInventoryItem>> {

    let __body: any = body;
    let __params = this.newParams();
    let __headers = new HttpHeaders();

    let req = new HttpRequest<any>(
      'POST',
      this.rootUrl + `/inventory/distinct`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      }
    );

    return this.http.request<any>(req).pipe(
      __filter(_r => {
        //console.log(_r);
        return _r instanceof HttpResponse;
      }),
      __map((_r: HttpResponse<any>) => {
        //console.log(_r);
        return _r as StrictHttpResponse<DistinctInventoryItem>;
      })
    );

  }

  updateInventoryItem(id: string, params: UpdateInventoryParams): Observable<DistinctInventoryItem> {
    return this.updateInventoryItemResponse(id, params).pipe(
      __map(r => r.body)
    )
  }

  updateInventoryItemResponse(id: string, params: UpdateInventoryParams): Observable<StrictHttpResponse<DistinctInventoryItem>> {

    let __params = this.newParams();
    let __headers = new HttpHeaders();

    let req = new HttpRequest<any>(
      'PUT',
      this.rootUrl + `/inventory/distinct/${id}`,
      params,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      }
    );

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<DistinctInventoryItem>;
      })
    );

  }

  deleteInventoryItem(id: string):  Observable<void> {
    return this.deleteInventoryItemResponse(id).pipe(
      __map(r => r.body)
    )
  }

  deleteInventoryItemResponse(id: string):  Observable<StrictHttpResponse<void>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();

    let req = new HttpRequest<any>(
      'DELETE',
      this.rootUrl + `/inventory/distinct/${id}`,
      null,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      }
    );

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<void>;
      })
    );

  }


}

module DistinctInventoryService {

  export interface GetInventoryParams {
    search?: string;
    offset?: number;
    count?: number;
    userId?: string;
    profileId?: string;
  }

  export interface UpdateInventoryParams {
    userId: string;
    profileId: string;
    metadata: any;
  }

}
