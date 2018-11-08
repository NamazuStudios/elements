import {CollectionViewer, DataSource} from "@angular/cdk/collections";
import {BehaviorSubject, Observable, of} from "rxjs";
import {catchError, finalize} from "rxjs/operators";
import {GameOnPrizesService} from "./api/services/game-on-prizes.service";
import {Prize} from "./api/models/prize";

export class GameOnPrizesDataSource implements DataSource<Prize> {

  private gameOnPrizesSubject = new BehaviorSubject<Prize[]>([]);
  private loadingSubject = new BehaviorSubject<boolean>(false);

  public loading$ = this.loadingSubject.asObservable();
  public gameOnPrizes$ = this.gameOnPrizesSubject.asObservable();

  constructor(private gameOnPrizesService: GameOnPrizesService) { }

  connect(collectionViewer: CollectionViewer): Observable<Prize[]> {
    return this.gameOnPrizes$;
  }

  disconnect(collectionViewer: CollectionViewer): void {
    this.gameOnPrizesSubject.complete();
    this.loadingSubject.complete();
  }

  // amazon API does not allow paging
  loadGameOnPrizes(applicationId: string, configurationId: string) {
    this.loadingSubject.next(true);

    this.gameOnPrizesService.getPrizes({ applicationId: applicationId, configurationId: configurationId })
      .pipe(
        catchError(() => of({ prizes: [], total: 0 })),
        finalize(() => this.loadingSubject.next(false)))
      .subscribe(gameOnPrizes => {
        this.gameOnPrizesSubject.next(gameOnPrizes.prizes);
      });
  }
}
