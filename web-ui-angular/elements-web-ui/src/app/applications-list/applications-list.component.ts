import {AfterViewInit, ChangeDetectorRef, Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {ApplicationsService} from "../api/services/applications.service";
import {ApplicationsDataSource} from "../applications.datasource";
import {MatPaginator} from "@angular/material/paginator";
import {debounceTime, distinctUntilChanged, filter, tap} from "rxjs/operators";
import {fromEvent} from "rxjs";
import {SelectionModel} from "@angular/cdk/collections";
import {Application} from "../api/models/application";
import {MatDialog, MatTable} from "@angular/material";
import {AlertService} from "../alert.service";
import {ConfirmationDialogService} from "../confirmation-dialog/confirmation-dialog.service";
import {ApplicationDialogComponent} from "../application-dialog/application-dialog.component";
import {ApplicationViewModel} from "../models/application-view-model";

@Component({
  selector: 'app-applications-list',
  templateUrl: './applications-list.component.html',
  styleUrls: ['./applications-list.component.css']
})
export class ApplicationsListComponent implements OnInit, AfterViewInit {
  hasSelection = false;
  selection: SelectionModel<Application>;
  dataSource: ApplicationsDataSource;
  displayedColumns= ["select", "id", "name", "actions"];
  currentApplications: Application[];

  @ViewChild(MatPaginator) paginator: MatPaginator;
  @ViewChild('input') input: ElementRef;
  @ViewChild(MatTable) table: MatTable<Application>;

  constructor(private applicationsService: ApplicationsService, private alertService: AlertService, private dialogService: ConfirmationDialogService, public dialog: MatDialog) { }

  ngOnInit() {
    this.selection = new SelectionModel<Application>(true, []);
    this.dataSource = new ApplicationsDataSource(this.applicationsService);
    this.paginator.pageSize = 10;
    this.refresh();
  }

  ngAfterViewInit() {
    // server-side search
    fromEvent(this.input.nativeElement,'keyup')
      .pipe(
        debounceTime(150),
        distinctUntilChanged(),
        tap(() => {
          this.paginator.pageIndex = 0;
          this.refresh();
        })
      )
      .subscribe();

    this.paginator.page
      .pipe(
        tap(() => this.refresh())
      )
      .subscribe();

    this.selection.onChange.subscribe(s => this.hasSelection = this.selection.hasValue());
    this.dataSource.applications$.subscribe(currentApplications => this.currentApplications = currentApplications);
    this.dataSource.totalCount$.subscribe(totalCount => this.paginator.length = totalCount);
  }

  // add support for searching here
  refresh() {
    this.selection.clear();
    this.dataSource.loadApplications(
      this.input.nativeElement.value,
      this.paginator.pageIndex,
      this.paginator.pageSize);
  }

  /** Whether the number of selected elements matches the total number of rows. */
  isAllSelected() {
    const numSelected = this.selection.selected.length;
    const numRows = this.currentApplications.length;
    return numSelected == numRows;
  }

  /** Selects all rows if they are not all selected; otherwise clear selection. */
  masterToggle() {
    this.isAllSelected() ?
      this.selection.clear() :
      this.currentApplications.forEach(row => this.selection.select(row));
  }

  deleteApplication(application) {
    this.dialogService
      .confirm('Confirm Dialog', `Are you sure you want to delete the application '${application.name}'`)
      .pipe(filter(r => r))
      .subscribe(res => {
        this.doDeleteApplication(application);
        setTimeout(() => this.refresh(), 500);
      });
  }

  doDeleteApplication(application) {
    this.applicationsService.deleteApplication(application.id).subscribe(r => {},
      error => this.alertService.error(error));
  }

  deleteSelectedApplications(){
    this.dialogService
      .confirm('Confirm Dialog', `Are you sure you want to delete the ${this.selection.selected.length} selected application${this.selection.selected.length==1 ? '' : 's'}?`)
      .pipe(filter(r => r))
      .subscribe(res => {
        this.selection.selected.forEach(row => this.doDeleteApplication(row));
        this.selection.clear();
        setTimeout(() => this.refresh(), 500);
      });
  }

  showDialog(action: string, application: Application) {
    const dialogRef = this.dialog.open(ApplicationDialogComponent, {
      width: '500px',
      data: { action: action, application: application }
    });

    dialogRef.afterClosed().subscribe(result => {
      console.log(result);
//    this.refresh();
    });
  }

  addApplication() {
    this.showDialog("New", new ApplicationViewModel());
  }

  editApplication(application) {
    this.showDialog("Edit", application);
  }
}
