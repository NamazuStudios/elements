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

  createInventoryItem(param: { itemId: string; userId: string; profileId?: string }) : Observable<StrictHttpResponse<DistinctInventoryItem>> {
    // TODO: Implement This Method
    return null;
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

}
