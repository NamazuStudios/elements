import { CollectionViewer, DataSource } from "@angular/cdk/collections";
import { BehaviorSubject, Observable, of } from "rxjs";
import { catchError, finalize } from "rxjs/operators";

import { Vault } from "./api/models/omni/vaults";
import { VaultsService } from "./api/services/blockchain/omni/vaults.service";
import { AuthenticationService } from "./authentication.service";

export class VaultsDataSource implements DataSource<Vault> {
  private vaultsSubject = new BehaviorSubject<Vault[]>([]);
  private totalCountSubject = new BehaviorSubject<number>(0);
  private loadingSubject = new BehaviorSubject<boolean>(false);

  public vaults$ = this.vaultsSubject.asObservable();
  public loading$ = this.loadingSubject.asObservable();
  public totalCount$ = this.totalCountSubject.asObservable();

  constructor(
    private vaultsService: VaultsService,
    private authService?: AuthenticationService,
  ) {}

  connect(collectionViewer: CollectionViewer): Observable<Vault[]> {
    return this.vaults$;
  }

  disconnect(collectionViewer: CollectionViewer): void {
    this.vaultsSubject.complete();
    this.loadingSubject.complete();
  }

  loadVaults(
    offset: number | null,
    count: number | null,
    userId?: string | null,
    userType?: string,
  ) {
    this.loadingSubject.next(true);

    // add search when ready
    this.vaultsService
      .getVaults({ offset, count, userId })
      .pipe(
        catchError(() => of({ objects: [], total: 0 })),
        finalize(() => this.loadingSubject.next(false))
      )
      .subscribe((vaults) => {
        let objects = vaults.objects;
        if (userType === 'YOURS' && this.authService) {
          objects = vaults.objects.filter((vault) => {
            return this.authService.currentSession.session.user.id === vault.user.id;
          });
        } else if (userType === 'USERS' && this.authService) {
          objects = vaults.objects.filter((vault) => {
            return vault.user.level === 'USER';
          });
        } else if (userType === 'SUPERUSERS' && this.authService) {
          objects = vaults.objects.filter((vault) => {
            return vault.user.level === 'SUPERUSER';
          });
        }
        this.vaultsSubject.next(objects);
        this.totalCountSubject.next(vaults.total);
      });
  }
}
