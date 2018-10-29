import {AfterViewInit, Component, ElementRef, Input, OnInit, ViewChild} from '@angular/core';
import {MatPaginator} from "@angular/material/paginator";
import {debounceTime, distinctUntilChanged, filter, tap} from "rxjs/operators";
import {fromEvent} from "rxjs";
import {SelectionModel} from "@angular/cdk/collections";
import {MatDialog, MatTable} from "@angular/material";
import {AlertService} from "../alert.service";
import {ConfirmationDialogService} from "../confirmation-dialog/confirmation-dialog.service";
import {ApplicationConfigurationViewModel} from "../models/application-configuration-view-model";
import {ApplicationConfiguration} from "../api/models/application-configuration";
import {ApplicationConfigurationsDataSource} from "../application-configuration.datasource";
import {ApplicationConfigurationsService} from "../api/services/application-configurations.service";
import {FacebookApplicationConfigurationService} from "../api/services/facebook-application-configuration.service";
import {FirebaseApplicationConfigurationService} from "../api/services/firebase-application-configuration.service";
import {FacebookApplicationConfigurationDialogComponent} from "../facebook-application-configuration-dialog/facebook-application-configuration-dialog.component";
import {FacebookApplicationConfigurationViewModel} from "../models/facebook-application-configuration-view-model";

@Component({
  selector: 'app-application-configurations-list',
  templateUrl: './application-configurations-list.component.html',
  styleUrls: ['./application-configurations-list.component.css']
})
export class ApplicationConfigurationsListComponent implements OnInit, AfterViewInit {
  @Input() applicationNameOrId: string;

  hasSelection = false;
  selection: SelectionModel<ApplicationConfiguration>;
  dataSource: ApplicationConfigurationsDataSource;
  displayedColumns= ["select", "id", "category", "uniqueIdentifier", "actions"];
  currentApplicationConfigurations: ApplicationConfiguration[];

  @ViewChild(MatPaginator) paginator: MatPaginator;
  @ViewChild('input') input: ElementRef;
  @ViewChild(MatTable) table: MatTable<ApplicationConfiguration>;

  constructor(private applicationConfigurationsService: ApplicationConfigurationsService,
              private alertService: AlertService,
              private dialogService: ConfirmationDialogService,
              public dialog: MatDialog,
              private facebookApplicationConfigurationService: FacebookApplicationConfigurationService,
              private firebaseApplicationConfigurationService: FirebaseApplicationConfigurationService) { }

  ngOnInit() {
    this.selection = new SelectionModel<ApplicationConfiguration>(true, []);
    this.dataSource = new ApplicationConfigurationsDataSource(this.applicationConfigurationsService);
    this.paginator.pageSize = 10;
    this.refresh(0);
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
    this.dataSource.applicationConfigurations$.subscribe(currentApplicationConfigurations => this.currentApplicationConfigurations = currentApplicationConfigurations);
    this.dataSource.totalCount$.subscribe(totalCount => this.paginator.length = totalCount);
  }

  refresh(delay = 500) {
    setTimeout(() => {
      this.selection.clear();
      this.dataSource.loadApplicationConfigurations(
        this.applicationNameOrId,
        this.input.nativeElement.value,
        this.paginator.pageIndex * this.paginator.pageSize,
        this.paginator.pageSize);
    }, delay)
  }

  /** Whether the number of selected elements matches the total number of rows. */
  isAllSelected() {
    const numSelected = this.selection.selected.length;
    const numRows = this.currentApplicationConfigurations.length;
    return numSelected == numRows;
  }

  /** Selects all rows if they are not all selected; otherwise clear selection. */
  masterToggle() {
    this.isAllSelected() ?
      this.selection.clear() :
      this.currentApplicationConfigurations.forEach(row => this.selection.select(row));
  }

  deleteApplicationConfiguration(applicationConfiguration) {
    this.dialogService
      .confirm('Confirm Dialog', `Are you sure you want to delete the applicationConfiguration '${applicationConfiguration.name}'`)
      .pipe(filter(r => r))
      .subscribe(res => {
        this.doDeleteApplicationConfiguration(applicationConfiguration);
        this.refresh();
      });
  }

  doDeleteApplicationConfiguration(applicationConfiguration) {
    // this.applicationConfigurationsService.deleteApplicationConfiguration(applicationConfiguration.id).subscribe(r => {},
    //   error => this.alertService.error(error));
  }

  deleteSelectedApplicationConfigurations(){
    this.dialogService
      .confirm('Confirm Dialog', `Are you sure you want to delete the ${this.selection.selected.length} selected applicationConfiguration${this.selection.selected.length==1 ? '' : 's'}?`)
      .pipe(filter(r => r))
      .subscribe(res => {
        this.selection.selected.forEach(row => this.doDeleteApplicationConfiguration(row));
        this.selection.clear();
        this.refresh();
      });
  }

  // showDialog(isNew: boolean, applicationConfiguration: ApplicationConfiguration, next) {
  //   const dialogRef = this.dialog.open(ApplicationConfigurationDialogComponent, {
  //     width: '500px',
  //     data: { isNew: isNew, applicationConfiguration: applicationConfiguration }
  //   });
  //
  //   dialogRef
  //     .afterClosed()
  //     .pipe(filter(r => r))
  //     .subscribe(next);
  // }

  addApplicationConfiguration(category: string) {
    switch(category) {
      case 'FACEBOOK':

          const dialogRef = this.dialog.open(FacebookApplicationConfigurationDialogComponent, {
            width: '500px',
            data: { isNew: true, applicationConfiguration: new FacebookApplicationConfigurationViewModel() }
          });

          dialogRef
            .afterClosed()
            .pipe(filter(r => r))
            .subscribe(result => {
                this.facebookApplicationConfigurationService.createFacebookApplicationConfiguration({ applicationNameOrId: this.applicationNameOrId, body: result }).subscribe(r => {
                    this.refresh();
                  },
                  error => this.alertService.error(error));
              });

        break;
    }

    // this.showDialog(true, new ApplicationConfigurationViewModel(),result => {
    //   this.applicationConfigurationsService.createApplicationConfiguration(result).subscribe(r => {
    //       this.refresh();
    //     },
    //     error => this.alertService.error(error));
    // });
  }

  editApplicationConfiguration(applicationConfiguration) {
    // this.showDialog(false, applicationConfiguration, result => {
    //   this.applicationConfigurationsService.updateApplicationConfiguration({ nameOrId: applicationConfiguration.id, body: result }).subscribe(r => {
    //       this.refresh();
    //     },
    //     error => this.alertService.error(error));
    // });
  }
}
