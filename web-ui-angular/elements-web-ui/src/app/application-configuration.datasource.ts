import {CollectionViewer, DataSource} from "@angular/cdk/collections";
import {BehaviorSubject, Observable, of} from "rxjs";
import {catchError, finalize} from "rxjs/operators";
import {ApplicationConfiguration} from "./api/models/application-configuration";
import {ApplicationConfigurationsService} from "./api/services/application-configurations.service";

export class ApplicationConfigurationsDataSource implements DataSource<ApplicationConfiguration> {

  private applicationsSubject = new BehaviorSubject<ApplicationConfiguration[]>([]);
  private totalCountSubject = new BehaviorSubject<number>(0);
  private loadingSubject = new BehaviorSubject<boolean>(false);

  public loading$ = this.loadingSubject.asObservable();
  public totalCount$= this.totalCountSubject.asObservable();
  public applicationConfigurations$ = this.applicationsSubject.asObservable();

  constructor(private applicationConfigurationsService: ApplicationConfigurationsService) { }

  connect(collectionViewer: CollectionViewer): Observable<ApplicationConfiguration[]> {
    return this.applicationConfigurations$;
  }

  disconnect(collectionViewer: CollectionViewer): void {
    this.applicationsSubject.complete();
    this.loadingSubject.complete();
  }

  // add in search when ready
  loadApplicationConfigurations(applicationNameOrId: string, search:string, offset: number, count: number) {
    this.loadingSubject.next(true);

    // add search when ready
    this.applicationConfigurationsService.getApplicationProfiles({ applicationNameOrId: applicationNameOrId, offset: offset, count: count })
      .pipe(
        catchError(() => of({ objects: [], total: 0 })),
        finalize(() => this.loadingSubject.next(false)))
      .subscribe(applications => {
        this.applicationsSubject.next(applications.objects);
        this.totalCountSubject.next(applications.total);
      });
  }
}
