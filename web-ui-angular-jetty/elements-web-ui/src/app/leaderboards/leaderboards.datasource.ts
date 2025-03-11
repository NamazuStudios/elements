import {CollectionViewer, DataSource} from "@angular/cdk/collections";
import {BehaviorSubject, Observable, of} from "rxjs";
import {catchError, finalize} from "rxjs/operators";

import { LeaderboardsService } from "../api/services/leaderboards.service";
import { Leaderboard } from "../api/models/leaderboard";

export class LeaderboardsDataSource implements DataSource<Leaderboard> {

  private leaderboardsSubject = new BehaviorSubject<Leaderboard[]>([]);
  private totalCountSubject = new BehaviorSubject<number>(0);
  private loadingSubject = new BehaviorSubject<boolean>(false);

  public leaderboards$ = this.leaderboardsSubject.asObservable();
  public loading$ = this.loadingSubject.asObservable();
  public totalCount$= this.totalCountSubject.asObservable();

  constructor(private leaderboardsService: LeaderboardsService) { }

  connect(collectionViewer: CollectionViewer): Observable<Leaderboard[]> {
    return this.leaderboards$;
  }

  disconnect(collectionViewer: CollectionViewer): void {
    this.leaderboardsSubject.complete();
    this.loadingSubject.complete();
  }

  // add in search when ready
  loadLeaderboards(search:string, offset: number, count: number) {
    this.loadingSubject.next(true);

    // add search when ready
    this.leaderboardsService.getLeaderboards({ offset: offset, count: count, search: search })
      .pipe(
        catchError(() => of({ objects: [], total: 0 })),
        finalize(() => this.loadingSubject.next(false)))
      .subscribe(leaderboards => {
        this.leaderboardsSubject.next(leaderboards.objects);
        this.totalCountSubject.next(leaderboards.total);
      });
  }
}
