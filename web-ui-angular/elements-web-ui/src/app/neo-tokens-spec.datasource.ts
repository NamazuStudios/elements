import { CollectionViewer, DataSource } from "@angular/cdk/collections";
import { BehaviorSubject, Observable, of } from "rxjs";
import { catchError, finalize } from "rxjs/operators";

import { NeoToken } from "./api/models/blockchain/neo-token";
import { NeoTokenSpecsService } from "./api/services/blockchain/neo-token-specs.service";

export class NeoTokensSpecDataSource implements DataSource<NeoToken> {
  private tokensSubject = new BehaviorSubject<NeoToken[]>([]);
  private totalCountSubject = new BehaviorSubject<number>(0);
  private loadingSubject = new BehaviorSubject<boolean>(false);

  public tokens$ = this.tokensSubject.asObservable();
  public loading$ = this.loadingSubject.asObservable();
  public totalCount$ = this.totalCountSubject.asObservable();

  constructor(private neoTokenSpecsService: NeoTokenSpecsService) {}

  connect(collectionViewer: CollectionViewer): Observable<NeoToken[]> {
    return this.tokens$;
  }

  disconnect(collectionViewer: CollectionViewer): void {
    this.tokensSubject.complete();
    this.loadingSubject.complete();
  }

  loadTokens(
    offset: number | null,
    count: number | null,
  ) {
    this.loadingSubject.next(true);

    this.neoTokenSpecsService
      .getTokenTemplates({ offset: offset, count: count })
      .pipe(
        catchError(() => of({ objects: [], total: 0 })),
        finalize(() => this.loadingSubject.next(false))
      )
      .subscribe((tokens) => {
        this.tokensSubject.next(tokens.objects);
        this.totalCountSubject.next(tokens.total);
      });
  }
}
