import {CollectionViewer, DataSource} from '@angular/cdk/collections';
import {Item} from './api/models/item';
import {ItemsService} from './api/services/items.service';
import {BehaviorSubject, Observable, of} from 'rxjs';
import {catchError, finalize} from 'rxjs/operators';

export class ItemsDataSource implements DataSource<Item> {
  private itemsSubject = new BehaviorSubject<Item[]>([]);
  private totalCountSubject = new BehaviorSubject<number>(0);
  private loadingSubject = new BehaviorSubject<boolean>(false);

  public loading$ = this.loadingSubject.asObservable();
  public totalCount$ = this.totalCountSubject.asObservable();
  public items$ = this.itemsSubject.asObservable();

  constructor(private itemsService: ItemsService) { }

  connect(collectionViewer: CollectionViewer): Observable<Item[] | ReadonlyArray<Item>> {
    return this.items$;
  }

  disconnect(collectionViewer: CollectionViewer): void {
    this.itemsSubject.complete();
    this.loadingSubject.complete();
  }

  // add in search when ready
  loadItems(search: string, offset: number, count: number) {
    this.loadingSubject.next(true);

    // add search when ready
    this.itemsService.getItems({offset: offset, count: count, search: search})
      .pipe(
        catchError(() => of({objects: [], total: 0})),
        finalize(() => this.loadingSubject.next(false)))
      .subscribe(items => {
        this.itemsSubject.next(items.objects);
        this.totalCountSubject.next(items.total);
      });
  }
}
