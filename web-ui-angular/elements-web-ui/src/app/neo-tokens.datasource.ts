import { CollectionViewer, DataSource } from "@angular/cdk/collections";
import { BehaviorSubject, Observable, of } from "rxjs";
import { catchError, finalize } from "rxjs/operators";

import { NeoToken } from "./api/models/blockchain/neo-token";
import { NeoTokensService } from "./api/services/blockchain/neo-tokens.service";

export class NeoTokensDataSource implements DataSource<NeoToken> {
  private tokensSubject = new BehaviorSubject<NeoToken[]>([]);
  private totalCountSubject = new BehaviorSubject<number>(0);
  private loadingSubject = new BehaviorSubject<boolean>(false);

  public tokens$ = this.tokensSubject.asObservable();
  public loading$ = this.loadingSubject.asObservable();
  public totalCount$ = this.totalCountSubject.asObservable();

  constructor(private neoTokensService: NeoTokensService) {}

  connect(collectionViewer: CollectionViewer): Observable<NeoToken[]> {
    return this.tokens$;
  }

  disconnect(collectionViewer: CollectionViewer): void {
    this.tokensSubject.complete();
    this.loadingSubject.complete();
  }

  // add in search when ready
  loadTokens(
    offset: number | null,
    count: number | null,
    tags: string[] | null,
    mintStatus: string[] | ['NOT_MINTED'],
    query: string | null
  ) {
    this.loadingSubject.next(true);

    // add search when ready
    this.neoTokensService
      .getTokens({ tags: tags, query: query, offset: offset, count: count, mintStatus: mintStatus })
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
