import {AfterViewInit, Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {ProfilesDataSource} from "../profiles.datasource";
import {MatPaginator} from "@angular/material/paginator";
import {debounceTime, distinctUntilChanged, filter, tap} from "rxjs/operators";
import {fromEvent} from "rxjs";
import {SelectionModel} from "@angular/cdk/collections";
import {MatDialog, MatTable} from "@angular/material";
import {AlertService} from "../alert.service";
import {ConfirmationDialogService} from "../confirmation-dialog/confirmation-dialog.service";
import {ProfileDialogComponent} from "../profile-dialog/profile-dialog.component";
import {ProfileViewModel} from "../models/profile-view-model";
import {Profile} from '../api/models';
import {ProfilesService} from '../api/services/profiles.service';

@Component({
  selector: 'app-profiles-list',
  templateUrl: './profiles-list.component.html',
  styleUrls: ['./profiles-list.component.css']
})
export class ProfilesListComponent implements OnInit, AfterViewInit {
  hasSelection = false;
  selection: SelectionModel<Profile>;
  dataSource: ProfilesDataSource;
  displayedColumns = ["select", "id", "name", "userName", "userEmail", "applicationName", "actions"];
  currentProfiles: Array<Profile> = [];
  allApplications = [{name: "All"}];

  @ViewChild(MatPaginator) paginator: MatPaginator;
  @ViewChild('input') input: ElementRef;
  @ViewChild(MatTable) table: MatTable<Profile>;

  constructor(private profilesService: ProfilesService, private alertService: AlertService, private dialogService: ConfirmationDialogService, public dialog: MatDialog) { }

  ngOnInit() {
    this.selection = new SelectionModel<Profile>(true, []);
    this.dataSource = new ProfilesDataSource(this.profilesService);
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
    this.dataSource.profiles$.subscribe(currentProfiles => this.currentProfiles = currentProfiles);
    this.dataSource.totalCount$.subscribe(totalCount => this.paginator.length = totalCount);
  }

  refresh(delay = 500) {
    setTimeout(() => {
      this.selection.clear();
      this.dataSource.loadProfiles(
        this.input.nativeElement.value,
        this.paginator.pageIndex * this.paginator.pageSize,
        this.paginator.pageSize);
    }, delay)
  }

  /** Whether the number of selected elements matches the total number of rows. */
  isAllSelected() {
    const numSelected = this.selection.selected.length;
    const numRows = this.currentProfiles.length;
    return numSelected == numRows;
  }

  /** Selects all rows if they are not all selected; otherwise clear selection. */
  masterToggle() {
    this.isAllSelected() ?
      this.selection.clear() :
      this.currentProfiles.forEach(row => this.selection.select(row));
  }

  deleteProfile(profile) {
    this.dialogService
      .confirm('Confirm Dialog', `Are you sure you want to delete the application '${profile.name}'`)
      .pipe(filter(r => r))
      .subscribe(res => {
        this.doDeleteProfile(profile);
        this.refresh();
      });
  }

  doDeleteProfile(profile) {
    this.profilesService.deactivateProfile(profile.id).subscribe(r => {},
      error => this.alertService.error(error));
  }

  deleteSelectedProfiles(){
    this.dialogService
      .confirm('Confirm Dialog', `Are you sure you want to delete the ${this.selection.selected.length} selected profile${this.selection.selected.length==1 ? '' : 's'}?`)
      .pipe(filter(r => r))
      .subscribe(res => {
        this.selection.selected.forEach(row => this.doDeleteProfile(row));
        this.selection.clear();
        this.refresh();
      });
  }

  showDialog(isNew: boolean, profile: Profile, next) {
    this.dialog.open(ProfileDialogComponent, {
      width: '900px',
      data: { isNew: isNew, profile: profile, next: next, refresher: this }
    });
  }

  addProfile() {
    this.showDialog(true, new ProfileViewModel(), result => {
      return this.profilesService.createProfile(result);
    });
  }

  editProfile(profile) {
    this.showDialog(false, profile, result => {
      return this.profilesService.updateProfile({ profileId: profile.id, body: result });
    });
  }
}
