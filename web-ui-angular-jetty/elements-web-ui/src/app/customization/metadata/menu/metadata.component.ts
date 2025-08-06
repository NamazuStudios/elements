import {SelectionModel} from '@angular/cdk/collections';
import {Component, OnInit, ViewChild} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {MatPaginator} from '@angular/material/paginator';
import {filter, tap} from 'rxjs/operators';
import {AlertService} from 'src/app/alert.service';
import {MetadataService} from 'src/app/api/services/metadata.service';
import {ConfirmationDialogService} from 'src/app/confirmation-dialog/confirmation-dialog.service';
import {
  MetadataDialogComponent
} from '../metadata-dialog/metadata-dialog.component';
import {
  MetadataDuplicateDialogComponent
} from './metadata-duplicate-dialog/metadata-duplicate-dialog.component';
import {MetadataDatasource} from '../metadata.datasource';
import {CreateMetadataRequest, Metadata, UpdateMetadataRequest} from '../../../api/models/metadata-tab';
import {ItemViewModel} from "../../../models/item-view-model";

@Component({
  selector: 'metadata',
  templateUrl: './metadata.component.html',
  styleUrls: ['./metadata.component.css']
})
export class MetadataComponent implements OnInit {

  hasSelection = false;
  dataSource: MetadataDatasource;
  selection: SelectionModel<Metadata>;
  templates = [];
  displayedColumns: Array<string> = [
    "select",
    "id",
    "name",
    "edit-action",
    "copy-action",
    "remove-action"
  ];

  @ViewChild(MatPaginator) paginator: MatPaginator;

  constructor(
    private metadataService: MetadataService,
    private dialogService: ConfirmationDialogService,
    private alertService: AlertService,
    public dialog: MatDialog,
  ) {
  }

  ngOnInit() {
    this.selection = new SelectionModel<Metadata>(true, []);
    this.dataSource = new MetadataDatasource(this.metadataService);
    this.dataSource.loadSpecs(null, null);
  }

  ngAfterViewInit() {
    this.selection.changed.subscribe(s => this.hasSelection = this.selection.hasValue());
    this.dataSource.metadata$.subscribe(
      (tokenSpecs) => {
        this.templates = tokenSpecs;
      }
    );
    this.dataSource.totalCount$.subscribe(
      (totalCount) => (this.paginator.length = totalCount)
    );
    this.paginator.page.pipe(tap(() => this.refresh())).subscribe();
  }

  createMetadata() {
    let newMetadata = {
      id: "",
      name: "",
      accessLevel: undefined,
      metadata: null,
      metadataSpec: undefined
    }

    this.showDialog(newMetadata, (result: Metadata) => {

      let request: CreateMetadataRequest = {
        accessLevel: result.accessLevel,
        metadataSpec: result.metadataSpec,
        metadata: result.metadata,
        name: result.name
      }

      return this.metadataService.createMetadata(request);
    });
  }

  updateMetadata(template: Metadata) {
    this.showDialog(template, (result: Metadata) => {

      let request: UpdateMetadataRequest = {
        accessLevel: result.accessLevel,
        metadataSpec: result.metadataSpec,
        metadata: result.metadata,
      }

      return this.metadataService.updateMetadata(template.id, request);
    });
  }

  showDialog(template: Metadata, next) {
    this.dialog.open(MetadataDialogComponent, {
      width: "800px",
      maxHeight: "90vh",
      data: {
        metadata: template,
        refresh: this.refresh.bind(this),
        isNew: template == null,
        next: next
      },
    });
  }

  refresh(delay = 500) {
    setTimeout(() => {
      this.selection.clear();
      this.dataSource.loadSpecs(
        this.paginator.pageIndex * this.paginator.pageSize,
        this.paginator.pageSize,
      );
    }, delay);
  }

  /** Whether the number of selected elements matches the total number of rows. */
  isAllSelected() {
    const numSelected = this.selection.selected.length;
    const numRows = this.templates.length;
    return numSelected == numRows;
  }

  /** Selects all rows if they are not all selected; otherwise clear selection. */
  masterToggle() {
    this.isAllSelected() ?
      this.selection.clear() :
      this.templates.forEach(row => this.selection.select(row));
  }

  openDuplicateModal(template: Metadata) {
    this.dialog.open(MetadataDuplicateDialogComponent, {
      width: "450px",
      maxHeight: "90vh",
      data: {
        submit: this.duplicateTemplate.bind(this, template),
      },
    });
  }

  duplicateTemplate(template: Metadata, name: string) {
    this.metadataService.createMetadata({
      name: name,
      accessLevel: template.accessLevel,
      metadataSpec: template.metadataSpec,
      metadata: template.metadata
    })
    .subscribe(() => {
      this.refresh();
    });
  }

  removeTemplate(template: Metadata) {
    this.metadataService.deleteMetadata(template.id).subscribe(
      (r) => {
        this.refresh();
      },
      (error) => this.alertService.error(error)
    );
  }

  confirmTemplateRemove(template: Metadata) {
    this.dialogService
      .confirm(
        "Confirm Dialog",
        `Are you sure you want to remove the metadata spec ${template.name}?`
      )
      .pipe(filter((r) => r))
      .subscribe(() => {
        this.removeTemplate(template);
        this.refresh();
      });
  }

  confirmSelectedTemplatesRemove() {
    this.dialogService
      .confirm(
        "Confirm Dialog",
        `Are you sure you want to remove ${
          this.selection.selected.length} selected metadata spec${
            this.selection.selected.length == 1 ? "" : "s"
        }?`
      )
      .pipe(filter((r) => r))
      .subscribe(() => {
        this.selection.selected.forEach(template => this.removeTemplate(template));
        this.refresh();
      });
  }

  confirmRebuildDialog() {
    this.dialogService
      .confirm(
        "Confirm Reindexing",
        `Reindexing a large database can take a very long time depending on Its size. Do you wish to proceed?`
      )
      .pipe(filter((r) => r))
      .subscribe(() => {

        this.metadataService.callReindex().subscribe(
          (r) => {
            this.refresh();
          },
          (error) => this.alertService.error(error)
        );
      });
  }
}
