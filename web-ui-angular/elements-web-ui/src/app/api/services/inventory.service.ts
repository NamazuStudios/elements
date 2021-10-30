/* tslint:disable */
import { Injectable } from '@angular/core';
import {ApiConfiguration} from '../api-configuration';
import {HttpClient, HttpHeaders, HttpRequest, HttpResponse} from '@angular/common/http';
import {BaseService} from '../base-service';
import {Observable} from 'rxjs';
import {StrictHttpResponse} from '../strict-http-response';
import {filter as __filter, map as __map} from 'rxjs/operators';
import {InventoryItem} from '../models/inventory-item';
import {PaginationInventoryItem} from '../models/pagination-inventory-item';
import {User} from '../models/user';
import { InventoryItemAdvanced } from '../models/inventory-item-advanced';

@Injectable({
  providedIn: 'root'
})
export class InventoryService extends BaseService {
  constructor(
    config: ApiConfiguration,
    http: HttpClient
  ) {
    super(config, http);
  }

  /**
   * Searches all inventory items in the system and returns the metadata for all matches against the given search filter.
   * @param params The 'InventoryService.GetInventoryParams' containing the following parameters:
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
  getInventoryResponseAdvanced(params: InventoryService.GetInventoryParams): Observable<StrictHttpResponse<PaginationInventoryItem>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;
    //(params.tags || []).forEach((val, index) => {if (val != null) __params = __params.append('tags', val.toString())});
    if (params.offset != null) __params = __params.set('offset', params.offset.toString());
    if (params.count != null) __params = __params.set('count', params.count.toString());
    if (params.userId != null) __params = __params.set('userId', params.userId.toString());
    if (params.search != null) __params = __params.set('search', params.search.toString());
    let req = new HttpRequest<any>(
      'GET',
      this.rootUrl + `/inventory/advanced`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<PaginationInventoryItem>;
      })
    );
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
  getInventoryAdvanced(params: InventoryService.GetInventoryParams): Observable<PaginationInventoryItem> {
    return this.getInventoryResponseAdvanced(params).pipe(
      __map(_r => _r.body)
    );
  }
  
  /**
   * Supplying an inventoryItems listing object, this will create a new inventoryItems listing with a newly assigned unique id.
   * The InventoryItemViewModel representation returned in the response body is a representation of the InventoryItemViewModel as persisted with a unique identifier
   * signed and with its fields properly normalized.
   * @param body undefined
   * @return successful operation
   */
  createInventoryItemResponseAdvanced(body?: InventoryItemAdvanced): Observable<StrictHttpResponse<InventoryItem>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;
    __body = body;

    let req = new HttpRequest<any>(
      'POST',
      this.rootUrl + `/inventory/advanced`,
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
        return _r as StrictHttpResponse<InventoryItem>;
      })
    );
  }

  /**
   * Supplying an inventoryItems item object, this will create a new inventoryItems item with a newly assigned unique id.
   * The InventoryItemViewModel representation returned in the response body is a representation of the InventoryItemViewModel as persisted with a unique identifier
   * signed and with its fields properly normalized.
   * @param body undefined
   * @return successful operation
   */
  createInventoryItemAdvanced(body?: InventoryItemAdvanced): Observable<InventoryItem> {
    return this.createInventoryItemResponseAdvanced(body).pipe(
      __map(_r => _r.body)
    );
  }



/**
   * Supplying an inventoryItems item, this will update the InventoryItemViewModel identified by the identifier in the path with contents from the passed in request body.
   * @param params The `InventoryService.UpdateInventoryAdvancedParams` containing the following parameters:
   *
   * - `identifier`:
   *
   * - `body`:
   *
   * @return successful operation
   */
 updateInventoryItemResponseAdvanced(params: InventoryService.UpdateInventoryAdvancedParams): Observable<StrictHttpResponse<InventoryItem>> {
  let __params = this.newParams();
  let __headers = new HttpHeaders();
  let __body: any = null;

  __body = params.body;
  let req = new HttpRequest<any>(
    'PUT',
    this.rootUrl + `/inventory/advanced/${params.identifier}`,
    __body,
    {
      headers: __headers,
      params: __params,
      responseType: 'json'
    }
  );
  return this.http.request<any>(req).pipe(
    __filter(_r => _r instanceof HttpResponse),
    __map((_r: HttpResponse<any>) => {
      return _r as StrictHttpResponse<InventoryItem>;
    })
  );
}

/**
 * Supplying an inventoryItems item, this will update the InventoryItemViewModel identified by the identifier in the path with contents from the passed in request body.
 * @param params The `InventoryService.UpdateInventoryAdvancedParams` containing the following parameters:
 *
 * - `identifier`:
 *
 * - `body`:
 *
 * @return successful operation
 */
 updateInventoryItemAdvanced(params: InventoryService.UpdateInventoryAdvancedParams): Observable<InventoryItem> {
  return this.updateInventoryItemResponseAdvanced(params).pipe(
    __map(_r => _r.body)
  );
}

/**
   * Supplying an inventoryItems item, this will update the InventoryItemViewModel identified by the identifier in the path with contents from the passed in request body.
   * @param params The `InventoryService.AdjustInventoryAdvancedParams` containing the following parameters:
   *
   * - `identifier`:
   *
   * - `body`:
   *
   * @return successful operation
   */
 adjustInventoryItemResponseAdvanced(params: InventoryService.AdjustInventoryAdvancedParams): Observable<StrictHttpResponse<InventoryItem>> {
  let __params = this.newParams();
  let __headers = new HttpHeaders();
  let __body: any = null;

  __body = params.body;
  let req = new HttpRequest<any>(
    'PATCH',
    this.rootUrl + `/inventory/advanced/${params.identifier}`,
    __body,
    {
      headers: __headers,
      params: __params,
      responseType: 'json'
    }
  );
  return this.http.request<any>(req).pipe(
    __filter(_r => _r instanceof HttpResponse),
    __map((_r: HttpResponse<any>) => {
      return _r as StrictHttpResponse<InventoryItem>;
    })
  );
}

/**
 * Supplying an inventoryItems item, this will update the InventoryItemViewModel identified by the identifier in the path with contents from the passed in request body.
 * @param params The `InventoryService.AdjustInventoryAdvancedParams` containing the following parameters:
 *
 * - `identifier`:
 *
 * - `body`:
 *
 * @return successful operation
 */
 adjustInventoryItemAdvanced(params: InventoryService.AdjustInventoryAdvancedParams): Observable<InventoryItem> {
  return this.adjustInventoryItemResponseAdvanced(params).pipe(
    __map(_r => _r.body)
  );
}


  /**
   * Looks up an inventoryItems item by the passed in identifier
   * @param identifier undefined
   * @return successful operation
   */
  getInventoryItemByIdentifierResponseAdvanced(identifier: string): Observable<StrictHttpResponse<InventoryItem>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    let req = new HttpRequest<any>(
      'GET',
      this.rootUrl + `/inventory/advanced/${identifier}`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<InventoryItem>;
      })
    );
  }

  /**
   * Looks up an inventoryItems item by the passed in identifier
   * @param identifier undefined
   * @return successful operation
   */
  getInventoryItemByIdentifierAdvanced(identifier: string): Observable<InventoryItem> {
    return this.getInventoryItemByIdentifierResponseAdvanced(identifier).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Deletes a specific inventoryItems item known to the server.
   * @param nameOrId undefined
   */
  deleteInventoryItemResponseAdvanced(nameOrId: string): Observable<StrictHttpResponse<void>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    let req = new HttpRequest<any>(
      "DELETE",
      this.rootUrl + `/inventory/advanced/${encodeURIComponent(nameOrId)}`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'text'
      }
    );

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r.clone({ body: null}) as StrictHttpResponse<void>
      })
    );
  }

  /**
   * Deletes a specific inventoryItems item known to the server.
   * @param nameOrId undefined
   */
  deleteInventoryItemAdvanced(nameOrId: string): Observable<void> {
    return this.deleteInventoryItemResponseAdvanced(nameOrId).pipe(
      __map(_r => _r.body)
    );
  }
}

module InventoryService {

  /**
   * Parameters for getInventory
   */
  export interface GetInventoryParams {
    //tags?: Array<string>;  // TODO: delete this as it is no longer in API and is probably depricated. 
    search?: string;
    offset?: number;
    count?: number;
    userId?: string;
  }

  /**
   * Parameters for adjustInventory
   */
  export interface AdjustInventoryAdvancedParams {
    identifier: string;
    body?: InventoryItemAdvancedAdjust;
  }

  export interface InventoryItemAdvancedAdjust {
    userId: string;
    quantityDelta: number;
    priority: number;
  }

  /**
   * Parameters for updateInventory
   */
  export interface UpdateInventoryAdvancedParams {
    identifier: string;
    body?: InventoryItemAdvancedUpdate;
  }

  export interface InventoryItemAdvancedUpdate {
    quantity: number;
  }
}
