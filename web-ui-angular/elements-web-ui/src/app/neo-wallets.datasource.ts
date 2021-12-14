import {CollectionViewer, DataSource} from "@angular/cdk/collections";
import {BehaviorSubject, Observable, of} from "rxjs";
import {catchError, finalize} from "rxjs/operators";

import { NeoWallet } from "./api/models/blockchain/neo-wallet";
import { NeoWalletsService } from "./api/services";

export class NeoWalletsDataSource implements DataSource<NeoWallet> {

  private walletsSubject = new BehaviorSubject<NeoWallet[]>([]);
  private totalCountSubject = new BehaviorSubject<number>(0);
  private loadingSubject = new BehaviorSubject<boolean>(false);

  public wallets$ = this.walletsSubject.asObservable();
  public loading$ = this.loadingSubject.asObservable();
  public totalCount$= this.totalCountSubject.asObservable();

  constructor(private neoWalletsService: NeoWalletsService) { }

  connect(collectionViewer: CollectionViewer): Observable<NeoWallet[]> {
    return this.wallets$;
  }

  disconnect(collectionViewer: CollectionViewer): void {
    this.walletsSubject.complete();
    this.loadingSubject.complete();
  }

  // add in search when ready
  loadWallets(offset: number | null, count: number | null, userId: string | null, format: string | null) {
    this.loadingSubject.next(true);

    // add search when ready
    this.neoWalletsService.getWallets({ offset: offset, count: count, userId: userId, format: format })
      .pipe(
        catchError(() => of({ objects: [], total: 0 })),
        finalize(() => this.loadingSubject.next(false)))
      .subscribe(wallets => {
        this.walletsSubject.next(wallets.objects);
        this.totalCountSubject.next(wallets.total);
      });
  }
}
