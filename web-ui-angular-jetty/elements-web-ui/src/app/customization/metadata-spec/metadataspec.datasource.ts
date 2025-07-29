import {CollectionViewer, DataSource} from "@angular/cdk/collections";
import {BehaviorSubject, Observable, of} from "rxjs";
import {catchError, finalize} from "rxjs/operators";
import {MetadataSpec} from "../../api/models/metadata-spec-tab";
import {MetadataSpecsService} from "../../api/services/metadata-specs.service";

export class MetadataSpecDatasource implements DataSource<MetadataSpec> {

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
    this.metadataSpecsService.getMetadataSpecs({ offset: offset, count: count})
      .pipe(
        catchError(() => of({ objects: [], total: 0 })),
        finalize(() => this.loadingSubject.next(false)))
      .subscribe(specs => {
        this.metadataSpecsSubject.next(specs.objects);
        this.totalCountSubject.next(specs.total);
      });
  }
}

// import { CollectionViewer, DataSource } from "@angular/cdk/collections";
// import { BehaviorSubject, Observable, of } from "rxjs";
// import { catchError, finalize } from "rxjs/operators";
//
// import { MetadataSpecsService } from "../../api/services/metadata-specs.service";
// import {MetadataSpec} from "../../api/models/token-spec-tab";
//
// export class NeoTokensSpecDataSource implements DataSource<MetadataSpec> {
//   private tokensSubject = new BehaviorSubject<MetadataSpec[]>([]);
//   private totalCountSubject = new BehaviorSubject<number>(0);
//   private loadingSubject = new BehaviorSubject<boolean>(false);
//
//   public tokens$ = this.tokensSubject.asObservable();
//   public loading$ = this.loadingSubject.asObservable();
//   public totalCount$ = this.totalCountSubject.asObservable();
//
//   constructor(private metadataSpecsService: MetadataSpecsService) {}
//
//   connect(collectionViewer: CollectionViewer): Observable<MetadataSpec[]> {
//     return this.tokens$;
//   }
//
//   disconnect(collectionViewer: CollectionViewer): void {
//     this.tokensSubject.complete();
//     this.loadingSubject.complete();
//   }
//
//   loadTemplates(
//     offset: number | null,
//     count: number | null,
//   ) {
//     this.loadingSubject.next(true);
//
//     this.metadataSpecsService
//       .getTokenTemplates({ offset: offset, count: count })
//       .pipe(
//         catchError(() => of({ objects: [], total: 0 })),
//         finalize(() => this.loadingSubject.next(false))
//       )
//       .subscribe((tokens) => {
//         this.tokensSubject.next(tokens.objects);
//         this.totalCountSubject.next(tokens.total);
//       });
//   }
// }
