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
import { NeoSmartContract } from "../../models/blockchain/neo-smart-contract";
import { PatchNeoSmartContractRequest } from "../../models/blockchain/patch-neo-smart-contract-request";
import { PaginationNeoSmartContract } from "../../models/blockchain/pagination-neo-smart-contract";

@Injectable({
  providedIn: "root",
})
class SmartContractsService extends BaseService {
  constructor(config: ApiConfiguration, http: HttpClient) {
    super(config, http);
  }

  readonly network = "NEO";

  /**
   * Gets a pagination of Neo Contracts.
   * @param params The `NeoSmartContractsService.GetNeoSmartContractsParams` containing the following parameters:
   *
   * - `query`:
   *
   * - `offset`:
   *
   * - `count`:
   *
   * @return successful operation
   */
  getNeoSmartContractsResponse(
    params: NeoSmartContractsService.GetNeoSmartContractsParams
  ): Observable<StrictHttpResponse<PaginationNeoSmartContract>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;
    if (params.query != null)
      __params = __params.set("search", params.query.toString());
    if (params.offset != null)
      __params = __params.set("offset", params.offset.toString());
    if (params.count != null)
      __params = __params.set("count", params.count.toString());
    let req = new HttpRequest<any>(
      "GET",
      this.rootUrl + `/blockchain/neo/contract`,
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
        return _r as StrictHttpResponse<PaginationNeoSmartContract>;
      })
    );
  }

  /**
   * Gets a pagination of Neo Contracts.
   * @param params The `NeoSmartContractsService.GetNeoSmartContractsParams` containing the following parameters:
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
  getNeoSmartContracts(
    params: NeoSmartContractsService.GetNeoSmartContractsParams
  ): Observable<PaginationNeoSmartContract> {
    return this.getNeoSmartContractsResponse(params).pipe(
      __map((_r) => _r.body)
    );
  }

  /**
   * Deletes a Neo Smart Contract with the specified contractId.
   * @param contractId undefined
   */
  deleteNeoSmartContractResponse(
    contractId: string
  ): Observable<StrictHttpResponse<void>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    let req = new HttpRequest<any>(
      "DELETE",
      this.rootUrl + `/blockchain/neo/contract/${contractId}`,
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
   * Deletes a Neo Smart Contract with the specified contractId.
   * @param contractId undefined
   */
  deleteNeoSmartContract(contractId: string): Observable<void> {
    return this.deleteNeoSmartContractResponse(contractId).pipe(
      __map((_r) => _r.body)
    );
  }

  /**
   * Gets a specific Neo Smart Contract by contractId.
   * @param contractId undefined
   * @return successful operation
   */
  getNeoSmartContractResponse(
    contractId: string
  ): Observable<StrictHttpResponse<NeoSmartContract>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;

    let req = new HttpRequest<any>(
      "GET",
      this.rootUrl + `/blockchain/neo/contract/${contractId}`,
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
        return _r as StrictHttpResponse<NeoSmartContract>;
      })
    );
  }

  /**
   * Gets a specific Neo Smart Contract by contractId.
   * @param contractId undefined
   * @return successful operation
   */
  getNeoSmartContract(contractId: string): Observable<NeoSmartContract> {
    return this.getNeoSmartContractResponse(contractId).pipe(
      __map((_r) => _r.body)
    );
  }

  /**
   * Patches a Neo Smart Contract entry, associated with the specified deployed script hash.
   * @param body undefined
   * @return successful operation
   */
  patchNeoSmartContractResponse(
    body?: PatchNeoSmartContractRequest
  ): Observable<StrictHttpResponse<NeoSmartContract>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;
    __body = body;
    let req = new HttpRequest<any>(
      "PATCH",
      this.rootUrl + `/blockchain/neo/contract`,
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
        return _r as StrictHttpResponse<NeoSmartContract>;
      })
    );
  }

  /**
   * Patches a Neo Smart Contract entry, associated with the specified deployed script hash.
   * @param body undefined
   * @return successful operation
   */
  patchNeoSmartContract(
    body?: PatchNeoSmartContractRequest
  ): Observable<NeoSmartContract> {
    return this.patchNeoSmartContractResponse(body).pipe(
      __map((_r) => _r.body)
    );
  }
}

module NeoSmartContractsService {
  /**
   * Parameters for getNeoSmartContracts
   */
  export interface GetNeoSmartContractsParams {
    offset?: number;
    count?: number;
    tags?: Array<string>;
    query?: string;
  }
}

export { NeoSmartContractsService };
