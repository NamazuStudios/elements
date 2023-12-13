import { SelectionModel } from '@angular/cdk/collections';
import { Component, OnInit, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatPaginator } from '@angular/material/paginator';
import { filter, tap } from 'rxjs/operators';
import { AlertService } from 'src/app/alert.service';
import { MetadataSpecsService } from 'src/app/api/services/metadata-specs.service';
import { ConfirmationDialogService } from 'src/app/confirmation-dialog/confirmation-dialog.service';
import { NeoSmartTokenSpecsDialogComponent } from 'src/app/neo-smart-token-specs-dialog/neo-smart-token-specs-dialog.component';
import { NeoSmartTokenSpecsDuplicateDialogComponent } from 'src/app/neo-smart-token-specs-duplicate-dialog/neo-smart-token-specs-duplicate-dialog.component';
import { NeoTokensSpecDataSource } from 'src/app/neo-tokens-spec.datasource';
import {MetadataSpec, MetadataSpecPropertyType} from '../../api/models/token-spec-tab';

@Component({
  selector: 'app-neo-smart-token-specs',
  templateUrl: './neo-smart-token-specs.component.html',
  styleUrls: ['./neo-smart-token-specs.component.css']
})
export class NeoSmartTokenSpecsComponent implements OnInit {

  hasSelection = false;
  dataSource: NeoTokensSpecDataSource;
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
    this.dataSource = new NeoTokensSpecDataSource(this.metadataSpecsService);
    this.dataSource.loadTemplates(null, null);
  }

  ngAfterViewInit() {
    this.selection.changed.subscribe(s => this.hasSelection = this.selection.hasValue());
    this.dataSource.tokens$.subscribe(
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
    this.dialog.open(NeoSmartTokenSpecsDialogComponent, {
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
      this.dataSource.loadTemplates(
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
    this.dialog.open(NeoSmartTokenSpecsDuplicateDialogComponent, {
      width: "450px",
      maxHeight: "90vh",
      data: {
        submit: this.duplicateTemplate.bind(this, template),
      },
    });
  }

  duplicateTemplate(template: MetadataSpec, name: string) {
    this.metadataSpecsService.createTokenSpec({
      name: name,
      type: MetadataSpecPropertyType.STRING,  // TODO
      properties: template.properties,
    })
    .subscribe(() => {
      this.refresh();
    });
  }

  removeTemplate(template: MetadataSpec) {
    this.metadataSpecsService.deleteTokenTemplate(template.id).subscribe(
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
        `Are you sure you want to remove the token spec ${template.name}?`
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
          this.selection.selected.length} selected token spec${
            this.selection.selected.length == 1 ? "" : "s"
        }?`
      )
      .pipe(filter((r) => r))
      .subscribe(() => {
        this.selection.selected.forEach(template => this.removeTemplate(template));
        this.refresh();
      });
  }
}
