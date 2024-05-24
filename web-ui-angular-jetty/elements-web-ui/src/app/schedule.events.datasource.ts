import {CollectionViewer, DataSource} from '@angular/cdk/collections';
import {BehaviorSubject, Observable, of} from 'rxjs';
import {catchError, finalize} from 'rxjs/operators';
import {ScheduleEvent} from "./api/models/schedule-event";
import {ScheduleEventsService} from "./api/services/schedule-events.service";

export class ScheduleEventsDatasource implements DataSource<ScheduleEvent> {
  private scheduleEventsSubject = new BehaviorSubject<ScheduleEvent[]>([]);
  private totalCountSubject = new BehaviorSubject<number>(0);
  private loadingSubject = new BehaviorSubject<boolean>(false);

  public loading$ = this.loadingSubject.asObservable();
  public totalCount$ = this.totalCountSubject.asObservable();
  public scheduleEvents$ = this.scheduleEventsSubject.asObservable();

  constructor(private scheduleEventsService: ScheduleEventsService) { }

  connect(collectionViewer: CollectionViewer): Observable<ScheduleEvent[] | ReadonlyArray<ScheduleEvent>> {
    return this.scheduleEvents$;
  }

  disconnect(collectionViewer: CollectionViewer): void {
    this.scheduleEventsSubject.complete();
    this.loadingSubject.complete();
  }

  // add in search when ready
  loadScheduleEvents(scheduleId: string, offset: number, count: number) {
    this.loadingSubject.next(true);

    // add search when ready
    this.scheduleEventsService.getScheduleEvents({offset: offset, count: count, search: null, scheduleNameOrId: scheduleId, tags: null})
      .pipe(
        catchError(() => of({objects: [], total: 0})),
        finalize(() => this.loadingSubject.next(false)))
      .subscribe(scheduleEvents => {
        this.scheduleEventsSubject.next(scheduleEvents.objects);
        this.totalCountSubject.next(scheduleEvents.total);
      });
  }
}
