import {CollectionViewer, DataSource} from "@angular/cdk/collections";
import {BehaviorSubject, Observable, of} from "rxjs";
import {catchError, finalize} from "rxjs/operators";
import {Metadata} from "../../api/models/metadata-tab";
import {MetadataService} from "../../api/services/metadata.service";

export class MetadataDatasource implements DataSource<Metadata> {

  private metadataSubject = new BehaviorSubject<Metadata[]>([]);
  private totalCountSubject = new BehaviorSubject<number>(0);
  private loadingSubject = new BehaviorSubject<boolean>(false);

  public metadata$ = this.metadataSubject.asObservable();
  public loading$ = this.loadingSubject.asObservable();
  public totalCount$= this.totalCountSubject.asObservable();

  constructor(private metadataService: MetadataService) { }

  connect(collectionViewer: CollectionViewer): Observable<Metadata[]> {
    return this.metadata$;
  }

  disconnect(collectionViewer: CollectionViewer): void {
    this.metadataSubject.complete();
    this.loadingSubject.complete();
  }

  loadSpecs(offset: number, count: number) {
    this.loadingSubject.next(true);

    // add search when ready
    this.metadataService.getMetadatas({ offset: offset, count: count})
      .pipe(
        catchError(() => of({ objects: [], total: 0 })),
        finalize(() => this.loadingSubject.next(false)))
      .subscribe(specs => {
        this.metadataSubject.next(specs.objects);
        this.totalCountSubject.next(specs.total);
      });
  }
}
