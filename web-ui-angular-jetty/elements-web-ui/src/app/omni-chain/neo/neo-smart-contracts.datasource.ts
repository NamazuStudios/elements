import { CollectionViewer, DataSource } from "@angular/cdk/collections";
import { BehaviorSubject, Observable, of } from "rxjs";
import { catchError, finalize } from "rxjs/operators";
import { NeoSmartContract } from "../../api/models/blockchain/neo-smart-contract";
import { NeoSmartContractsService } from "../../api/services/blockchain/neo-smart-contracts.service";

export class NeoSmartContractsDataSource implements DataSource<NeoSmartContract> {
  private neoSmartContractSubject = new BehaviorSubject<NeoSmartContract[]>([]);
  private totalCountSubject = new BehaviorSubject<number>(0);
  private loadingSubject = new BehaviorSubject<boolean>(false);

  public neoSmartContracts$ = this.neoSmartContractSubject.asObservable();
  public loading$ = this.loadingSubject.asObservable();
  public totalCount$ = this.totalCountSubject.asObservable();

  constructor(private neoSmartContractsService: NeoSmartContractsService) {}

  connect(collectionViewer: CollectionViewer): Observable<NeoSmartContract[]> {
    return this.neoSmartContracts$;
  }

  disconnect(collectionViewer: CollectionViewer): void {
    this.neoSmartContractSubject.complete();
    this.loadingSubject.complete();
  }

  // add in search when ready
  loadNeoSmartContracts(
    offset: number | null,
    count: number | null,
    query: string | null
  ) {
    this.loadingSubject.next(true);

    // add search when ready
    this.neoSmartContractsService
      .getNeoSmartContracts({ query: query, offset: offset, count: count })
      .pipe(
        catchError(() => of({ objects: [], total: 0 })),
        finalize(() => this.loadingSubject.next(false))
      )
      .subscribe((tokens) => {
        this.neoSmartContractSubject.next(tokens.objects);
        this.totalCountSubject.next(tokens.total);
      });
  }
}
