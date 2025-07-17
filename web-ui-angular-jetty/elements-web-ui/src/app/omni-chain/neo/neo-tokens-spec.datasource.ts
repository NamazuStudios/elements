import { CollectionViewer, DataSource } from "@angular/cdk/collections";
import { BehaviorSubject, Observable, of } from "rxjs";
import { catchError, finalize } from "rxjs/operators";

import { MetadataSpecsService } from "../../api/services/metadata-specs.service";
import {MetadataSpec} from "../../api/models/token-spec-tab";
import {NeoTokensService} from "../../api/services/blockchain/neo-tokens.service";
import {NeoToken} from "../../api/models/blockchain/neo-token";

export class NeoTokensSpecDataSource implements DataSource<NeoToken> {
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

  loadTemplates(
    offset: number | null,
    count: number | null,
  ) {
    this.loadingSubject.next(true);

    this.neoTokensService
      .getTokens({ offset: offset, count: count })
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
