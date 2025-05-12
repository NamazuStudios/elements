import {AfterViewInit, Component, ElementRef, Input, OnInit, ViewChild} from '@angular/core';
import {MatPaginator} from '@angular/material/paginator';
import {debounceTime, distinctUntilChanged, filter, tap} from 'rxjs/operators';
import {fromEvent} from 'rxjs';
import {SelectionModel} from '@angular/cdk/collections';
import {MatDialog} from '@angular/material/dialog';
import {MatTable} from '@angular/material/table';
import {AlertService} from '../../alert.service';
import {ConfirmationDialogService} from '../../confirmation-dialog/confirmation-dialog.service';
import {ApplicationConfigurationViewModel} from '../../models/application-configuration-view-model';
import {ApplicationConfiguration} from '../../api/models/application-configuration';
import {ApplicationConfigurationsDataSource} from '../application-configuration.datasource';
import {ApplicationConfigurationsService} from '../../api/services/application-configurations.service';
import {FacebookApplicationConfigurationService} from '../../api/services/facebook-application-configuration.service';
import {FirebaseApplicationConfigurationService} from '../../api/services/firebase-application-configuration.service';
import {FacebookApplicationConfigurationDialogComponent} from '../facebook-application-configuration-dialog/facebook-application-configuration-dialog.component';
import {FirebaseApplicationConfigurationDialogComponent} from '../firebase-application-configuration-dialog/firebase-application-configuration-dialog.component';
import {MatchmakingApplicationConfigurationDialogComponent} from '../matchmaking-application-configuration-dialog/matchmaking-application-configuration-dialog.component';
import {MatchmakingApplicationConfigurationService} from '../../api/services/matchmaking-application-configuration.service';
import {IosApplicationConfigurationDialogComponent} from '../ios-application-configuration-dialog/ios-application-configuration-dialog.component';
import {AndroidGooglePlayConfigurationDialogComponent} from '../android-google-play-configuration-dialog/android-google-play-configuration-dialog.component';
import {IOSApplicationConfigurationService} from '../../api/services/iosapplication-configuration.service';
import {GooglePlayApplicationConfigurationService} from '../../api/services/google-play-application-configuration.service';

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
  displayedColumns = ['select', 'id', 'type', 'name', 'actions'];
  currentApplicationConfigurations: ApplicationConfiguration[];

  @ViewChild(MatPaginator) paginator: MatPaginator;
  @ViewChild('input') input: ElementRef;
  @ViewChild(MatTable) table: MatTable<ApplicationConfiguration>;

  constructor(private applicationConfigurationsService: ApplicationConfigurationsService,
              private alertService: AlertService,
              private dialogService: ConfirmationDialogService,
              public dialog: MatDialog,
              private facebookApplicationConfigurationService: FacebookApplicationConfigurationService,
              private firebaseApplicationConfigurationService: FirebaseApplicationConfigurationService,
              private matchmakingApplicationConfigurationService: MatchmakingApplicationConfigurationService,
              private iosApplicationConfigurationService: IOSApplicationConfigurationService,
              private googlePlayApplicationConfigurationService: GooglePlayApplicationConfigurationService) { }

  ngOnInit() {
    this.selection = new SelectionModel<ApplicationConfiguration>(true, []);
    this.dataSource = new ApplicationConfigurationsDataSource(this.applicationConfigurationsService);
    this.refresh(0);
  }

  ngAfterViewInit() {
    this.paginator.pageSize = 10;
    // server-side search
    fromEvent(this.input.nativeElement, 'keyup')
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

    this.selection.changed.subscribe(s => this.hasSelection = this.selection.hasValue());
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
    }, delay);
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

  shortenType(type: string): string {
    return type.split('.').pop() || type; // Extracts the last segment after the dot
  }

  deleteApplicationConfiguration(applicationConfiguration) {
    this.dialogService
      .confirm('Confirm Dialog', `Are you sure you want to delete the application configuration '${applicationConfiguration.uniqueIdentifier}'`)
      .pipe(filter(r => r))
      .subscribe(res => {
        this.doDeleteApplicationConfiguration(applicationConfiguration);
        this.refresh();
      });
  }

  doDeleteApplicationConfiguration(applicationConfiguration) {
    switch (applicationConfiguration.category) {
      case 'FACEBOOK':
        this.facebookApplicationConfigurationService.deleteFacebookApplicationConfiguration({applicationNameOrId: this.applicationNameOrId, applicationConfigurationNameOrId: applicationConfiguration.id}).subscribe(r => { },
          error => this.alertService.error(error));

        break;
      case 'FIREBASE':
        this.firebaseApplicationConfigurationService.deleteFirebaseApplicationConfiguration({applicationNameOrId: this.applicationNameOrId, applicationConfigurationNameOrId: applicationConfiguration.id}).subscribe(r => { },
          error => this.alertService.error(error));

        break;
      case 'MATCHMAKING':
        this.matchmakingApplicationConfigurationService.deleteMatchmakingApplicationConfiguration({applicationNameOrId: this.applicationNameOrId, applicationConfigurationNameOrId: applicationConfiguration.id}).subscribe(r => { },
          error => this.alertService.error(error));

        break;
      case 'IOS_APP_STORE':
        this.iosApplicationConfigurationService.deleteIosApplicationConfiguration({applicationNameOrId: this.applicationNameOrId, applicationConfigurationNameOrId: applicationConfiguration.id}).subscribe(r => {},
          error => this.alertService.error(error));

        break;
      case 'ANDROID_GOOGLE_PLAY':
        this.googlePlayApplicationConfigurationService.deleteGooglePlayApplicationConfiguration({applicationNameOrId: this.applicationNameOrId, applicationConfigurationNameOrId: applicationConfiguration.id}).subscribe(r => {},
          error => this.alertService.error(error));

        break;
    }

  }

  deleteSelectedApplicationConfigurations() {
    this.dialogService
      .confirm('Confirm Dialog', `Are you sure you want to delete the ${this.selection.selected.length} selected application configuration${this.selection.selected.length == 1 ? '' : 's'}?`)
      .pipe(filter(r => r))
      .subscribe(res => {
        this.selection.selected.forEach(row => this.doDeleteApplicationConfiguration(row));
        this.selection.clear();
        this.refresh();
      });
  }

  showDialog(isNew: boolean, dialog: any, applicationConfiguration: any, next, isBundleDialog: boolean = false) {
    const dialogRef = this.dialog.open(dialog, {
      width: isBundleDialog ? '900px' : '500px',
      data: { isNew: isNew, applicationConfiguration: applicationConfiguration, next: next, refresher: this }
    });

    dialogRef.afterClosed().pipe(filter(r => r)).subscribe(next);
  }

  addApplicationConfiguration(category: string) {
    switch (category) {
      case 'FACEBOOK':
          this.showDialog(true, FacebookApplicationConfigurationDialogComponent, { parent: { id: this.applicationNameOrId } }, result => {
            this.facebookApplicationConfigurationService.createFacebookApplicationConfiguration({ applicationNameOrId: this.applicationNameOrId, body: result }).subscribe(r => {
                this.refresh();
              },
              error => this.alertService.error(error));
          });

        break;
      case 'FIREBASE':
        this.showDialog(true, FirebaseApplicationConfigurationDialogComponent, { parent: { id: this.applicationNameOrId } }, result => {
          this.firebaseApplicationConfigurationService.createFirebaseApplicationConfiguration({ applicationNameOrId: this.applicationNameOrId, body: result }).subscribe(r => {
              this.refresh();
            },
            error => this.alertService.error(error));
        });

        break;
      case 'MATCHMAKING':
        this.showDialog(true, MatchmakingApplicationConfigurationDialogComponent, { parent: { id: this.applicationNameOrId }, success: { } }, result => {
          this.matchmakingApplicationConfigurationService.createMatchmakingApplicationConfiguration({ applicationNameOrId: this.applicationNameOrId, body: result }).subscribe(r => {
              this.refresh();
            },
            error => this.alertService.error(error));
        });

        break;
      case 'IOS_APP_STORE':
        this.showDialog(true, IosApplicationConfigurationDialogComponent, { parent: { id: this.applicationNameOrId } }, result => {
          this.iosApplicationConfigurationService.createIosApplicationConfiguration({ applicationNameOrId: this.applicationNameOrId, body: result }).subscribe(r => {
            this.refresh();
          },
            error => this.alertService.error(error));
        }, true);

        break;
      case 'ANDROID_GOOGLE_PLAY':
        this.showDialog(true, AndroidGooglePlayConfigurationDialogComponent, { parent: { id: this.applicationNameOrId } }, result => {
          this.googlePlayApplicationConfigurationService.createGooglePlayApplicationConfiguration({applicationNameOrId: this.applicationNameOrId, body: result}).subscribe(r => {
            this.refresh();
          },
            error => this.alertService.error(error));
        }, true);

        break;
    }
  }

  editApplicationConfiguration(applicationConfiguration) {
    switch (applicationConfiguration.category) {
      case 'FACEBOOK':
        this.facebookApplicationConfigurationService.getFacebookApplicationConfiguration({applicationNameOrId: this.applicationNameOrId, applicationConfigurationNameOrId: applicationConfiguration.id})
          .subscribe(applicationConfiguration =>
          this.showDialog(false, FacebookApplicationConfigurationDialogComponent, applicationConfiguration, result => {
            this.facebookApplicationConfigurationService.updateApplicationConfiguration({ applicationNameOrId: this.applicationNameOrId, applicationConfigurationNameOrId: applicationConfiguration.id, body: result }).subscribe(r => {
                this.refresh();
              },
              error => this.alertService.error(error));
          }));

        break;
      case 'FIREBASE':
        this.firebaseApplicationConfigurationService.getFirebaseApplicationConfiguration({applicationNameOrId: this.applicationNameOrId, applicationConfigurationNameOrId: applicationConfiguration.id})
          .subscribe(applicationConfiguration =>
            this.showDialog(false, FirebaseApplicationConfigurationDialogComponent, applicationConfiguration, result => {
              this.firebaseApplicationConfigurationService.updateApplicationConfiguration({ applicationNameOrId: this.applicationNameOrId, applicationConfigurationNameOrId: applicationConfiguration.id, body: result }).subscribe(r => {
                  this.refresh();
                },
                error => this.alertService.error(error));
            }));

        break;
      case 'MATCHMAKING':
        this.matchmakingApplicationConfigurationService.getMatchmakingApplicationConfiguration({applicationNameOrId: this.applicationNameOrId, applicationConfigurationNameOrId: applicationConfiguration.id})
          .subscribe(applicationConfiguration =>
            this.showDialog(false, MatchmakingApplicationConfigurationDialogComponent, applicationConfiguration, result => {
              this.matchmakingApplicationConfigurationService.updateApplicationConfiguration({ applicationNameOrId: this.applicationNameOrId, applicationConfigurationNameOrId: applicationConfiguration.id, body: result }).subscribe(r => {
                  this.refresh();
                },
                error => this.alertService.error(error));
            }));
      case 'IOS_APP_STORE':
        this.iosApplicationConfigurationService.getIosApplicationConfiguration({applicationNameOrId: this.applicationNameOrId, applicationConfigurationNameOrId: applicationConfiguration.id })
          .subscribe(applicationConfiguration => {
            this.showDialog(false, IosApplicationConfigurationDialogComponent, applicationConfiguration, result => {
              this.iosApplicationConfigurationService.updateApplicationConfiguration({applicationNameOrId: this.applicationNameOrId, applicationConfigurationNameOrId: applicationConfiguration.id, body: result }).subscribe(r => {
                  this.refresh();
                },
              error => this.alertService.error(error));
            }, true);
          });

        break;
      case 'ANDROID_GOOGLE_PLAY':
        this.googlePlayApplicationConfigurationService.getGooglePlayApplicationConfiguration({applicationNameOrId: this.applicationNameOrId, applicationConfigurationNameOrId: applicationConfiguration.id })
          .subscribe(applicationConfiguration => {
            this.showDialog(false, AndroidGooglePlayConfigurationDialogComponent, applicationConfiguration, result => {
              this.googlePlayApplicationConfigurationService.updateApplicationConfiguration({applicationNameOrId: this.applicationNameOrId, applicationConfigurationNameOrId: applicationConfiguration.id, body: result }).subscribe(r => {
                  this.refresh();
                },
                error => this.alertService.error(error));
            }, true);
          });

        break;
    }
  }
}
