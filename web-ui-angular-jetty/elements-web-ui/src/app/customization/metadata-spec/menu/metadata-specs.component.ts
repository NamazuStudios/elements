import {SelectionModel} from '@angular/cdk/collections';
import {Component, OnInit, ViewChild} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {MatPaginator} from '@angular/material/paginator';
import {filter, tap} from 'rxjs/operators';
import {AlertService} from 'src/app/alert.service';
import {MetadataSpecsService} from 'src/app/api/services/metadata-specs.service';
import {ConfirmationDialogService} from 'src/app/confirmation-dialog/confirmation-dialog.service';
import {
  MetadataSpecsDialogComponent
} from './metadata-specs-dialog/metadata-specs-dialog.component';
import {
  MetadataSpecsDuplicateDialogComponent
} from './metadata-specs-duplicate-dialog/metadata-specs-duplicate-dialog.component';
import {MetadataSpecDatasource} from '../metadataspec.datasource';
import {MetadataSpec, MetadataSpecPropertyType} from '../../../api/models/metadata-spec-tab';

@Component({
  selector: 'metadata-specs',
  templateUrl: './metadata-specs.component.html',
  styleUrls: ['./metadata-specs.component.css']
})
export class MetadataSpecsComponent implements OnInit {

  hasSelection = false;
  dataSource: MetadataSpecDatasource;
  selection: SelectionModel<MetadataSpec>;
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
    private metadataSpecsService: MetadataSpecsService,
    private dialogService: ConfirmationDialogService,
    private alertService: AlertService,
    public dialog: MatDialog,
  ) {}

  ngOnInit() {
    this.selection = new SelectionModel<MetadataSpec>(true, []);
    this.dataSource = new MetadataSpecDatasource(this.metadataSpecsService);
    this.dataSource.loadSpecs(null, null);
  }

  ngAfterViewInit() {
    this.selection.changed.subscribe(s => this.hasSelection = this.selection.hasValue());
    this.dataSource.metadataSpecs$.subscribe(
      (tokenSpecs) => {
        this.templates = tokenSpecs;
      }
    );
    this.dataSource.totalCount$.subscribe(
      (totalCount) => (this.paginator.length = totalCount)
    );
    this.paginator.page.pipe(tap(() => this.refresh())).subscribe();
  }

  showDialog(template: MetadataSpec) {
    this.dialog.open(MetadataSpecsDialogComponent, {
      width: "800px",
      maxHeight: "90vh",
      data: {
        template,
        refresh: this.refresh.bind(this),
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

  openDuplicateModal(template: MetadataSpec) {
    this.dialog.open(MetadataSpecsDuplicateDialogComponent, {
      width: "450px",
      maxHeight: "90vh",
      data: {
        submit: this.duplicateTemplate.bind(this, template),
      },
    });
  }

  duplicateTemplate(template: MetadataSpec, name: string) {
    this.metadataSpecsService.createMetadataSpec({
      name: name,
      type: MetadataSpecPropertyType.STRING,  // TODO
      properties: template.properties,
    })
    .subscribe(() => {
      this.refresh();
    });
  }

  removeTemplate(template: MetadataSpec) {
    this.metadataSpecsService.deleteMetadataSpec(template.id).subscribe(
      (r) => {
        this.refresh();
      },
      (error) => this.alertService.error(error)
    );
  }

  confirmTemplateRemove(template: MetadataSpec) {
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

        this.metadataSpecsService.callReindex().subscribe(
          (r) => {
            this.refresh();
          },
          (error) => this.alertService.error(error)
        );
      });
  }
}
