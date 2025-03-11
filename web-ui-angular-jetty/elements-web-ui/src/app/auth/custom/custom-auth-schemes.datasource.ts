import { CollectionViewer, DataSource } from "@angular/cdk/collections";
import { BehaviorSubject, Observable, of } from "rxjs";
import { catchError, finalize } from "rxjs/operators";
import { CustomAuthScheme } from "../../api/models/auth/auth-scheme-custom";
import { CustomAuthSchemesService } from "../../api/services/auth/custom-auth-schemes.service";


export class CustomAuthSchemesDataSource implements DataSource<CustomAuthScheme> {
  private authSchemesSubject = new BehaviorSubject<CustomAuthScheme[]>([]);
  private totalCountSubject = new BehaviorSubject<number>(0);
  private loadingSubject = new BehaviorSubject<boolean>(false);

  public authSchemes$ = this.authSchemesSubject.asObservable();
  public loading$ = this.loadingSubject.asObservable();
  public totalCount$ = this.totalCountSubject.asObservable();

  constructor(private authSchemesService: CustomAuthSchemesService) {}

  connect(collectionViewer: CollectionViewer): Observable<CustomAuthScheme[]> {
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
