import {Profile} from './api/models/profile';
import {DataSource} from '@angular/cdk/collections';
import {BehaviorSubject, Observable, of} from 'rxjs';
import {Application} from './api/models/application';
import {ApplicationsService} from './api/services/applications.service';
import {CollectionViewer} from '@angular/cdk/collections';
import {catchError, finalize} from 'rxjs/operators';
import {ProfilesService} from './api/services/profiles.service';

export class ProfilesDataSource  implements DataSource<Profile> {

  private profilesSubject = new BehaviorSubject<Profile[]>([]);
  private totalCountSubject = new BehaviorSubject<number>(0);
  private loadingSubject = new BehaviorSubject<boolean>(false);

  public loading$ = this.loadingSubject.asObservable();
  public totalCount$= this.totalCountSubject.asObservable();
  public profiles$ = this.profilesSubject.asObservable();

  constructor(private profilesService: ProfilesService) { }

  connect(collectionViewer: CollectionViewer): Observable<Profile[]> {
    return this.profiles$;
  }

  disconnect(collectionViewer: CollectionViewer): void {
    this.profilesSubject.complete();
    this.loadingSubject.complete();
  }

  // add in search when ready
  loadProfiles(search: string, offset: number, count: number) {
    this.loadingSubject.next(true);

    // add search when ready
    this.profilesService.getProfiles({ offset: offset, count: count, search: search })
      .pipe(
        catchError(() => of({ objects: [], total: 0 })),
        finalize(() => this.loadingSubject.next(false)))
      .subscribe(profiles => {
        this.profilesSubject.next(profiles.objects);
        this.totalCountSubject.next(profiles.total);
      });
  }
}
