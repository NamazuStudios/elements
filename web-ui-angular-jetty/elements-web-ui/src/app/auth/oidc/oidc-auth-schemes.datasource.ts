import { CollectionViewer, DataSource } from "@angular/cdk/collections";
import { BehaviorSubject, Observable, of } from "rxjs";
import { catchError, finalize } from "rxjs/operators";
import { OidcAuthScheme } from "../../api/models/auth/auth-scheme-oidc";
import {OidcAuthSchemesService} from "../../api/services/auth/oidc-auth-schemes.service";


export class OidcAuthSchemesDatasource implements DataSource<OidcAuthScheme> {
  private authSchemesSubject = new BehaviorSubject<OidcAuthScheme[]>([]);
  private totalCountSubject = new BehaviorSubject<number>(0);
  private loadingSubject = new BehaviorSubject<boolean>(false);

  public authSchemes$ = this.authSchemesSubject.asObservable();
  public loading$ = this.loadingSubject.asObservable();
  public totalCount$ = this.totalCountSubject.asObservable();

  constructor(private authSchemesService: OidcAuthSchemesService) {}

  connect(collectionViewer: CollectionViewer): Observable<OidcAuthScheme[]> {
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
