import { CollectionViewer, DataSource } from "@angular/cdk/collections";
import { BehaviorSubject, Observable, of } from "rxjs";
import { catchError, finalize } from "rxjs/operators";

import { Wallet } from "./api/models/omni/wallets";
import { WalletsService } from "./api/services/blockchain/omni/wallets.service";
import { AuthenticationService } from "./authentication.service";

export class WalletsDataSource implements DataSource<Wallet> {
  private walletsSubject = new BehaviorSubject<Wallet[]>([]);
  private totalCountSubject = new BehaviorSubject<number>(0);
  private loadingSubject = new BehaviorSubject<boolean>(false);

  public wallets$ = this.walletsSubject.asObservable();
  public loading$ = this.loadingSubject.asObservable();
  public totalCount$ = this.totalCountSubject.asObservable();

  constructor(
    private walletsService: WalletsService,
    private authService?: AuthenticationService,
  ) {}

  connect(collectionViewer: CollectionViewer): Observable<Wallet[]> {
    return this.wallets$;
  }

  disconnect(collectionViewer: CollectionViewer): void {
    this.walletsSubject.complete();
    this.loadingSubject.complete();
  }

  loadWallets(
    vaultId?: string,
    offset?: number | null,
    count?: number | null,
    userId?: string | null,
    userType?: string | null,
  ) {
    this.loadingSubject.next(true);

    // add search when ready
    this.walletsService
      .getWalletsByVault({ offset, count, userId, vaultId })
      .pipe(
        catchError(() => of({ objects: [], total: 0 })),
        finalize(() => this.loadingSubject.next(false))
      )
      .subscribe((wallets) => {
        let objects = wallets.objects;
        if (userType === 'YOURS' && this.authService) {
          objects = wallets.objects.filter((vault) => {
            return this.authService.currentSession.session.user.id === vault.user.id;
          });
        } else if (userType === 'USERS' && this.authService) {
          objects = wallets.objects.filter((vault) => {
            return vault.user.level === 'USER';
          });
        } else if (userType === 'SUPERUSERS' && this.authService) {
          objects = wallets.objects.filter((vault) => {
            return vault.user.level === 'SUPERUSER';
          });
        }
        this.walletsSubject.next(objects);
        this.totalCountSubject.next(wallets.total);
      });
  }
}
