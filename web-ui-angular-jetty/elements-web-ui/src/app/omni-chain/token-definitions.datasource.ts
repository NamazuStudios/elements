import { CollectionViewer, DataSource } from "@angular/cdk/collections";
import { BehaviorSubject, Observable, of } from "rxjs";
import { catchError, finalize } from "rxjs/operators";

import { NeoToken } from '../api/models/blockchain/neo-token';
import { TokenDefinition } from '../api/models/blockchain/token-definition';
import { TokenDefinitionService } from '../api/services/blockchain/token-definition.service';

export class TokenDefinitionsDataSource implements DataSource<TokenDefinition> {
  private tokensSubject = new BehaviorSubject<TokenDefinition[]>([]);
  private totalCountSubject = new BehaviorSubject<number>(0);
  private loadingSubject = new BehaviorSubject<boolean>(false);

  public tokens$ = this.tokensSubject.asObservable();
  public loading$ = this.loadingSubject.asObservable();
  public totalCount$ = this.totalCountSubject.asObservable();

  constructor(private tokenDefinitionService: TokenDefinitionService) {}

  connect(collectionViewer: CollectionViewer): Observable<TokenDefinition[]> {
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

    this.tokenDefinitionService
      .getTokenDefinitions({ offset: offset, count: count })
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
