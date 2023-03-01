import { CollectionViewer, DataSource } from "@angular/cdk/collections";
import { BehaviorSubject, Observable, of } from "rxjs";
import { catchError, finalize } from "rxjs/operators";
import { Contract } from "./api/models/omni/contracts";
import { ContractsService } from "./api/services/blockchain/omni/contracts.service";
import { AuthenticationService } from "./authentication.service";

export class ContractsDataSource implements DataSource<Contract> {
  private contractsSubject = new BehaviorSubject<Contract[]>([]);
  private totalCountSubject = new BehaviorSubject<number>(0);
  private loadingSubject = new BehaviorSubject<boolean>(false);

  public contracts$ = this.contractsSubject.asObservable();
  public loading$ = this.loadingSubject.asObservable();
  public totalCount$ = this.totalCountSubject.asObservable();

  constructor(
    private contractsService: ContractsService,
    private authService?: AuthenticationService,
  ) {}

  connect(collectionViewer: CollectionViewer): Observable<Contract[]> {
    return this.contracts$;
  }

  disconnect(collectionViewer: CollectionViewer): void {
    this.contractsSubject.complete();
    this.loadingSubject.complete();
  }

  loadContracts(
    offset?: number | null,
    count?: number | null,
    api?: string | null,
    network?: string[] | null,
    userType?: string | null,
  ) {
    this.loadingSubject.next(true);

    // add search when ready
    this.contractsService
      .getContracts({ offset, count, api, network })
      .pipe(
        catchError(() => of({ objects: [], total: 0 })),
        finalize(() => this.loadingSubject.next(false))
      )
      .subscribe((contracts) => {
        let objects = contracts.objects;
        if (userType === 'YOURS' && this.authService) {
          objects = contracts.objects.filter((vault) => {
            return this.authService.currentSession.session.user.id === vault.user.id;
          });
        } else if (userType === 'USERS' && this.authService) {
          objects = contracts.objects.filter((vault) => {
            return vault.user.level === 'USER';
          });
        } else if (userType === 'SUPERUSERS' && this.authService) {
          objects = contracts.objects.filter((vault) => {
            return vault.user.level === 'SUPERUSER';
          });
        }
        this.contractsSubject.next(objects);
        this.totalCountSubject.next(contracts.total);
      });
  }
}
