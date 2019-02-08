/* tslint:disable */
import { Injectable } from '@angular/core';
import { HttpClient, HttpRequest, HttpResponse, HttpHeaders, HttpParams } from '@angular/common/http';
import { BaseService } from '../base-service';
import { ApiConfiguration } from '../api-configuration';
import { StrictHttpResponse } from '../strict-http-response';
import { Observable } from 'rxjs';
import { map as __map, filter as __filter } from 'rxjs/operators';

import { PaginationItem } from '../models/pagination-item';
import { Item } from '../models/item';
import {Http} from '@angular/http';
@Injectable({
  providedIn: 'root',
})
class ItemsService extends BaseService {
  constructor(
    config: ApiConfiguration,
    http: HttpClient
  ) {
    super(config, http);
  }

  /**
   * Searches all items and returns all matching items, filtered by the passed in search parameters.  If multiple tags are specified, then all items that contain at least one of the passed in tags is returned.
   * @param params The `ItemsService.GetItemsParams` containing the following parameters:
   *
   * - `tags`:
   *
   * - `search`:
   *
   * - `offset`:
   *
   * - `count`:
   *
   * @return successful operation
   */
  getItemsResponse(params: ItemsService.GetItemsParams): Observable<StrictHttpResponse<PaginationItem>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;
    (params.tags || []).forEach((val, index) => {if (val != null) __params = __params.append('tags', val.toString())});
    if (params.search != null) __params = __params.set('search', params.search.toString());
    if (params.offset != null) __params = __params.set('offset', params.offset.toString());
    if (params.count != null) __params = __params.set('count', params.count.toString());
    let req = new HttpRequest<any>(
      'GET',
      this.rootUrl + `/item`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<PaginationItem>;
      })
    );
  }

  /**
   * Searches all items and returns all matching items, filtered by the passed in search parameters.  If multiple tags are specified, then all items that contain at least one of the passed in tags is returned.
   * @param params The `ItemsService.GetItemsParams` containing the following parameters:
   *
   * - `tags`:
   *
   * - `search`:
   *
   * - `offset`:
   *
   * - `count`:
   *
   * @return successful operation
   */
  getItems(params: ItemsService.GetItemsParams): Observable<PaginationItem> {
    return this.getItemsResponse(params).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Supplying an item object, this will create a new item with a newly assigned unique id.  The Item representation returned in the response body is a representation of the Item as persisted with a unique identifier signed and with its fields properly normalized.  The supplied item object submitted with the request must have a name property that is unique across all items.
   * @param body undefined
   * @return successful operation
   */
  createItemResponse(body?: Item): Observable<StrictHttpResponse<Item>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;
    __body = body;
    let req = new HttpRequest<any>(
      'POST',
      this.rootUrl + `/item`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<Item>;
      })
    );
  }

  /**
   * Supplying an item object, this will create a new item with a newly assigned unique id.  The Item representation returned in the response body is a representation of the Item as persisted with a unique identifier signed and with its fields properly normalized.  The supplied item object submitted with the request must have a name property that is unique across all items.
   * @param body undefined
   * @return successful operation
   */
  createItem(body?: Item): Observable<Item> {
    return this.createItemResponse(body).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Looks up an item by the passed in identifier
   * @param identifier undefined
   * @return successful operation
   */
  getItemByIdentifierResponse(identifier: string): Observable<StrictHttpResponse<Item>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    let req = new HttpRequest<any>(
      'GET',
      this.rootUrl + `/item/${identifier}`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<Item>;
      })
    );
  }

  /**
   * Looks up an item by the passed in identifier
   * @param identifier undefined
   * @return successful operation
   */
  getItemByIdentifier(identifier: string): Observable<Item> {
    return this.getItemByIdentifierResponse(identifier).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Supplying an item, this will update the Item identified by the identifier in the path with contents from the passed in request body.
   * @param params The `ItemsService.UpdateItemParams` containing the following parameters:
   *
   * - `identifier`:
   *
   * - `body`:
   *
   * @return successful operation
   */
  updateItemResponse(params: ItemsService.UpdateItemParams): Observable<StrictHttpResponse<Item>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    __body = params.body;
    let req = new HttpRequest<any>(
      'PUT',
      this.rootUrl + `/item/${params.identifier}`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<Item>;
      })
    );
  }

  /**
   * Supplying an item, this will update the Item identified by the identifier in the path with contents from the passed in request body.
   * @param params The `ItemsService.UpdateItemParams` containing the following parameters:
   *
   * - `identifier`:
   *
   * - `body`:
   *
   * @return successful operation
   */
  updateItem(params: ItemsService.UpdateItemParams): Observable<Item> {
    return this.updateItemResponse(params).pipe(
      __map(_r => _r.body)
    );
  }

  /**
   * Deletes a specific item known to the server.
   * @param nameOrId undefined
   */
  deleteItemResponse(nameOrId: string): Observable<StrictHttpResponse<void>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    let req = new HttpRequest<any>(
      "DELETE",
      this.rootUrl + `/item/${nameOrId}`,
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
   * Deletes a specific item known to the server.
   * @param nameOrId undefined
   */
  deleteItem(nameOrId: string): Observable<void> {
    return this.deleteItemResponse(nameOrId).pipe(
      __map(_r => _r.body)
    );
  }
}

module ItemsService {

  /**
   * Parameters for getItems
   */
  export interface GetItemsParams {
    tags?: Array<string>;
    search?: string;
    offset?: number;
    count?: number;
  }

  /**
   * Parameters for updateItem
   */
  export interface UpdateItemParams {
    identifier: string;
    body?: Item;
  }
}

export { ItemsService }
