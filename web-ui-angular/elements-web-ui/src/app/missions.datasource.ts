import {CollectionViewer, DataSource} from '@angular/cdk/collections';
import {BehaviorSubject, Observable, of} from 'rxjs';
import {catchError, finalize} from 'rxjs/operators';
import {Mission} from './api/models/mission';
import {MissionsService} from './api/services/missions.service';

export class MissionsDatasource implements DataSource<Mission> {
  private missionsSubject = new BehaviorSubject<Mission[]>([]);
  private totalCountSubject = new BehaviorSubject<number>(0);
  private loadingSubject = new BehaviorSubject<boolean>(false);

  public loading$ = this.loadingSubject.asObservable();
  public totalCount$ = this.totalCountSubject.asObservable();
  public missions$ = this.missionsSubject.asObservable();

  constructor(private missionsService: MissionsService) { }

  connect(collectionViewer: CollectionViewer): Observable<Mission[] | ReadonlyArray<Mission>> {
    return this.missions$;
  }

  disconnect(collectionViewer: CollectionViewer): void {
    this.missionsSubject.complete();
    this.loadingSubject.complete();
  }

  // add in search when ready
  loadMissions(search: string, offset: number, count: number) {
    this.loadingSubject.next(true);

    // add search when ready
    this.missionsService.getMissions({offset: offset, count: count, search: search})
      .pipe(
        catchError(() => of({objects: [], total: 0})),
        finalize(() => this.loadingSubject.next(false)))
      .subscribe(missions => {
        this.missionsSubject.next(missions.objects);
        this.totalCountSubject.next(missions.total);
      });
  }
}
