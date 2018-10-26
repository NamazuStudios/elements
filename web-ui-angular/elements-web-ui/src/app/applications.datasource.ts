import {CollectionViewer, DataSource} from "@angular/cdk/collections";
import {Application} from "./api/models/application";
import {ApplicationsService} from "./api/services/applications.service";
import {BehaviorSubject, Observable, of} from "rxjs";
import {catchError, finalize} from "rxjs/operators";

export class ApplicationsDataSource implements DataSource<Application> {

  private applicationsSubject = new BehaviorSubject<Application[]>([]);
  private totalCountSubject = new BehaviorSubject<number>(0);
  private loadingSubject = new BehaviorSubject<boolean>(false);

  public loading$ = this.loadingSubject.asObservable();
  public totalCount$= this.totalCountSubject.asObservable();
  public applications$ = this.applicationsSubject.asObservable();

  constructor(private applicationsService: ApplicationsService) { }

  connect(collectionViewer: CollectionViewer): Observable<Application[]> {
    return this.applications$;
  }

  disconnect(collectionViewer: CollectionViewer): void {
    this.applicationsSubject.complete();
    this.loadingSubject.complete();
  }

  // add in search when ready
  loadApplications(search:string, offset: number, count: number) {
    this.loadingSubject.next(true);

    // add search when ready
    this.applicationsService.getApplications({ offset: offset, count: count })
      .pipe(
        catchError(() => of({ objects: [], total: 0 })),
        finalize(() => this.loadingSubject.next(false)))
      .subscribe(applications => {
        this.applicationsSubject.next(applications.objects);
        this.totalCountSubject.next(applications.total);
      });
  }
}
