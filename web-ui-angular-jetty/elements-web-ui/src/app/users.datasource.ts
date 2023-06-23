import {CollectionViewer, DataSource} from "@angular/cdk/collections";
import {UsersService} from "./api/services/users.service";
import {BehaviorSubject, Observable, of} from "rxjs";
import {catchError, finalize} from "rxjs/operators";
import {User} from "./api/models/user";
import {PaginationUser} from "./api/models";

export class UsersDataSource implements DataSource<User> {

  private usersSubject = new BehaviorSubject<User[]>([]);
  private totalCountSubject = new BehaviorSubject<number>(0);
  private loadingSubject = new BehaviorSubject<boolean>(false);

  public users$ = this.usersSubject.asObservable();
  public loading$ = this.loadingSubject.asObservable();
  public totalCount$= this.totalCountSubject.asObservable();

  constructor(private usersService: UsersService) { }

  connect(collectionViewer: CollectionViewer): Observable<User[]> {
    return this.users$;
  }

  disconnect(collectionViewer: CollectionViewer): void {
    this.usersSubject.complete();
    this.loadingSubject.complete();
  }

  // add in search when ready
  loadUsers(search:string, offset: number, count: number) {
    this.loadingSubject.next(true);

    // add search when ready
    this.usersService.getUsers({ offset: offset, count: count, search: search })
      .pipe(
        catchError(() => of({ objects: [], total: 0 })),
        finalize(() => this.loadingSubject.next(false)))
      .subscribe(users => {
        this.usersSubject.next(users.objects);
        this.totalCountSubject.next(users.total);
      });
  }
}
