import { HttpClient, HttpHeaders, HttpRequest, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map as __map, filter as __filter } from 'rxjs/operators';
import { ApiConfiguration } from 'src/app/api/api-configuration';
import { BaseService } from 'src/app/api/base-service';
import { Contract, CreateContractRequest, UpdateContractRequest } from 'src/app/api/models/omni/contracts';
import { Pagination } from 'src/app/api/models/pagination';
import { StrictHttpResponse } from 'src/app/api/strict-http-response';

@Injectable({
  providedIn: 'root'
})
class ContractsService extends BaseService {
  constructor(
    config: ApiConfiguration,
    http: HttpClient
  ) {
    super(config, http);
  }

  private getContractsResponse(params: ContractsService.GetContractsParams): Observable<StrictHttpResponse<Pagination<Contract>>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body: any = null;
    if (params.offset != null) __params = __params.set('offset', params.offset.toString());
    if (params.count != null) __params = __params.set('count', params.count.toString());
    if (params.api != null) __params = __params.set('api', params.api.toString());
    if (params.network != null) __params = __params.set('network', params.network.toString());
    let req = new HttpRequest<any>(
      'GET',
       `${this.rootUrl}/blockchain/omni/smart_contract`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter(_r => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<Pagination<Contract>>;
      })
    );
  }

  getContracts(params: ContractsService.GetContractsParams): Observable<Pagination<Contract>> {
    return this.getContractsResponse(params).pipe(
      __map(_r => _r.body)
    );
  }

  private createContractResponse(body: CreateContractRequest): Observable<StrictHttpResponse<Contract>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body = body;
    let req = new HttpRequest<any>(
      'POST',
       `${this.rootUrl}/blockchain/omni/smart_contract`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter((_r) => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<Contract>;
      })
    );
  }

  createContract(body: CreateContractRequest): Observable<Contract> {
    return this.createContractResponse(body).pipe(
      __map(_r => _r.body)
    );
  }

  private editContractResponse(params: ContractsService.UpdateContractParams): Observable<StrictHttpResponse<Contract>> {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body = params.body;
    let req = new HttpRequest<any>(
      'PUT',
       `${this.rootUrl}/blockchain/omni/smart_contract/${params.contractId}`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter((_r) => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<Contract>;
      })
    );
  }

  editContract(params: ContractsService.UpdateContractParams) {
    return this.editContractResponse(params).pipe(
      __map(_r => _r.body)
    );
  }

  private deleteContractResponse(contractId: string) {
    let __params = this.newParams();
    let __headers = new HttpHeaders();
    let __body = null;
    let req = new HttpRequest<any>(
      'DELETE',
       `${this.rootUrl}/blockchain/omni/smart_contract/${contractId}`,
      __body,
      {
        headers: __headers,
        params: __params,
        responseType: 'json'
      });

    return this.http.request<any>(req).pipe(
      __filter((_r) => _r instanceof HttpResponse),
      __map((_r: HttpResponse<any>) => {
        return _r as StrictHttpResponse<Contract>;
      })
    );
  }

  deleteContract(contractId: string) {
    return this.deleteContractResponse(contractId).pipe(
      __map(_r => _r.body)
    );
  }
}

module ContractsService {
  export interface GetContractsParams {
    offset?: number;
    count?: number;
    api?: string;
    network?: string[];
  }

  export interface UpdateContractParams {
    contractId: string;
    body: UpdateContractRequest;
  }
}

export { ContractsService };
