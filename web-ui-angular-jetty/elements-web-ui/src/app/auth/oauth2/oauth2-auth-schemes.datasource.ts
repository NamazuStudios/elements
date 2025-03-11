import { CollectionViewer, DataSource } from "@angular/cdk/collections";
import { BehaviorSubject, Observable, of } from "rxjs";
import { catchError, finalize } from "rxjs/operators";
import { Oauth2AuthScheme } from "../../api/models/auth/auth-scheme-oauth2";
import { Oauth2AuthSchemesService } from "../../api/services/auth/oauth2-auth-schemes.service";


export class Oauth2AuthSchemesDatasource implements DataSource<Oauth2AuthScheme> {
  private authSchemesSubject = new BehaviorSubject<Oauth2AuthScheme[]>([]);
  private totalCountSubject = new BehaviorSubject<number>(0);
  private loadingSubject = new BehaviorSubject<boolean>(false);

  public authSchemes$ = this.authSchemesSubject.asObservable();
  public loading$ = this.loadingSubject.asObservable();
  public totalCount$ = this.totalCountSubject.asObservable();

  constructor(private authSchemesService: Oauth2AuthSchemesService) {}

  connect(collectionViewer: CollectionViewer): Observable<Oauth2AuthScheme[]> {
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
