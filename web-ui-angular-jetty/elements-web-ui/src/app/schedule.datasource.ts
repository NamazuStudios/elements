import {CollectionViewer, DataSource} from '@angular/cdk/collections';
import {BehaviorSubject, Observable, of} from 'rxjs';
import {catchError, finalize} from 'rxjs/operators';
import {Schedule} from "./api/models/schedule";
import {SchedulesService} from "./api/services/schedules.service";

export class SchedulesDatasource implements DataSource<Schedule> {
  private schedulesSubject = new BehaviorSubject<Schedule[]>([]);
  private totalCountSubject = new BehaviorSubject<number>(0);
  private loadingSubject = new BehaviorSubject<boolean>(false);

  public loading$ = this.loadingSubject.asObservable();
  public totalCount$ = this.totalCountSubject.asObservable();
  public schedules$ = this.schedulesSubject.asObservable();

  constructor(private schedulesService: SchedulesService) { }

  connect(collectionViewer: CollectionViewer): Observable<Schedule[] | ReadonlyArray<Schedule>> {
    return this.schedules$;
  }

  disconnect(collectionViewer: CollectionViewer): void {
    this.schedulesSubject.complete();
    this.loadingSubject.complete();
  }

  // add in search when ready
  loadSchedules(search: string, offset: number, count: number) {
    this.loadingSubject.next(true);

    // add search when ready
    this.schedulesService.getSchedules({offset: offset, count: count, search: search})
      .pipe(
        catchError(() => of({objects: [], total: 0})),
        finalize(() => this.loadingSubject.next(false)))
      .subscribe(schedules => {
        this.schedulesSubject.next(schedules.objects);
        this.totalCountSubject.next(schedules.total);
      });
  }
}
