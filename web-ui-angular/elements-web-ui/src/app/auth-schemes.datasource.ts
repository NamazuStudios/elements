import { CollectionViewer, DataSource } from "@angular/cdk/collections";
import { BehaviorSubject, Observable, of } from "rxjs";
import { catchError, finalize } from "rxjs/operators";
import { AuthScheme } from "./api/models/blockchain/authScheme";
import { AuthSchemesService } from "./api/services/blockchain/auth-schemes.service";


export class AuthSchemesDataSource implements DataSource<AuthScheme> {
  private authSchemesSubject = new BehaviorSubject<AuthScheme[]>([]);
  private totalCountSubject = new BehaviorSubject<number>(0);
  private loadingSubject = new BehaviorSubject<boolean>(false);

  public authSchemes$ = this.authSchemesSubject.asObservable();
  public loading$ = this.loadingSubject.asObservable();
  public totalCount$ = this.totalCountSubject.asObservable();

  constructor(private authSchemesService: AuthSchemesService) {}

  connect(collectionViewer: CollectionViewer): Observable<AuthScheme[]> {
    return this.authSchemes$;
  }

  disconnect(collectionViewer: CollectionViewer): void {
    this.authSchemesSubject.complete();
    this.loadingSubject.complete();
  }

  // add in search when ready
  loadAuthSchemes(
    offset: number | null,
    count: number | null,
    tags: string[] | null,
    query: string | null
  ) {
    this.loadingSubject.next(true);

    // add search when ready
    this.authSchemesService
      .getAuthSchemes({ tags: tags, query: query, offset: offset, count: count })
      .pipe(
        catchError(() => of({ objects: [], total: 0 })),
        finalize(() => this.loadingSubject.next(false))
      )
      .subscribe((authSchemes) => {
        this.authSchemesSubject.next(authSchemes.objects);
        this.totalCountSubject.next(authSchemes.total);
      });
  }
}
