import {AfterViewInit, Component, ElementRef, Inject, OnInit, ViewChild} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material/dialog';
import {MatPaginator} from '@angular/material/paginator';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatTable} from '@angular/material/table';
import {AlertService} from '../../../alert.service';
import {tap} from 'rxjs/operators';
import {MetadataSpecDatasource} from "../metadataspec.datasource";
import {MetadataSpec} from "../../../api/models/token-spec-tab";
import {MetadataSpecsService} from "../../../api/services/metadata-specs.service";

@Component({
  selector: 'app-metadataspec-select-dialog',
  templateUrl: './metadataspec-select-dialog.component.html',
  styleUrls: ['./metadataspec-select-dialog.component.css']
})
export class MetadataspecSelectDialogComponent implements OnInit, AfterViewInit {
  dataSource: MetadataSpecDatasource;
  displayedColumns = ["id", "name", "actions"];
  currentSpecs: MetadataSpec[];

  @ViewChild(MatPaginator) paginator: MatPaginator;
  @ViewChild('input') input: ElementRef;
  @ViewChild(MatTable) table: MatTable<MetadataSpec>;

  constructor(public dialogRef: MatDialogRef<MetadataspecSelectDialogComponent>, public dialog: MatDialog, @Inject(MAT_DIALOG_DATA) public data: any,
              private metadataSpecsService: MetadataSpecsService, private alertService: AlertService, private snackBar: MatSnackBar) { }

  ngOnInit() {
    this.dataSource = new MetadataSpecDatasource(this.metadataSpecsService);
    this.refresh(0);
  }

  ngAfterViewInit() {
    this.paginator.pageSize = 10;

    this.paginator.page
      .pipe(
        tap(() => this.refresh())
      )
      .subscribe();

    this.dataSource.metadataSpecs$.subscribe(specs => this.currentSpecs = specs);
    this.dataSource.totalCount$.subscribe(totalCount => this.paginator.length = totalCount);
  }

  refresh(delay = 500) {
    setTimeout(() => {
      this.dataSource.loadSpecs(
        this.paginator.pageIndex * this.paginator.pageSize,
        this.paginator.pageSize);
    }, delay);
  }

  close(spec?: MetadataSpec) {
    if(!spec) {
      this.dialogRef.close();
      return;
    }

    this.data.next(spec);
    this.dialogRef.close();
  }

}
