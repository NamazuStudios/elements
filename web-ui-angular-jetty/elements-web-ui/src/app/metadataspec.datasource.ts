import {CollectionViewer, DataSource} from "@angular/cdk/collections";
import {BehaviorSubject, Observable, of} from "rxjs";
import {catchError, finalize} from "rxjs/operators";
import {MetadataSpec} from "./api/models/token-spec-tab";
import {MetadataSpecsService} from "./api/services/metadata-specs.service";

export class MetadataspecDatasource implements DataSource<MetadataSpec> {

  private metadataSpecsSubject = new BehaviorSubject<MetadataSpec[]>([]);
  private totalCountSubject = new BehaviorSubject<number>(0);
  private loadingSubject = new BehaviorSubject<boolean>(false);

  public metadataSpecs$ = this.metadataSpecsSubject.asObservable();
  public loading$ = this.loadingSubject.asObservable();
  public totalCount$= this.totalCountSubject.asObservable();

  constructor(private metadataSpecsService: MetadataSpecsService) { }

  connect(collectionViewer: CollectionViewer): Observable<MetadataSpec[]> {
    return this.metadataSpecs$;
  }

  disconnect(collectionViewer: CollectionViewer): void {
    this.metadataSpecsSubject.complete();
    this.loadingSubject.complete();
  }

  loadSpecs(offset: number, count: number) {
    this.loadingSubject.next(true);

    // add search when ready
    this.metadataSpecsService.getTokenTemplates({ offset: offset, count: count})
      .pipe(
        catchError(() => of({ objects: [], total: 0 })),
        finalize(() => this.loadingSubject.next(false)))
      .subscribe(specs => {
        this.metadataSpecsSubject.next(specs.objects);
        this.totalCountSubject.next(specs.total);
      });
  }
}
