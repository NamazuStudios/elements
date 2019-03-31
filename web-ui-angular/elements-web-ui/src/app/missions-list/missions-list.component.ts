import {AfterViewInit, Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {SelectionModel} from '@angular/cdk/collections';
import {MatDialog, MatPaginator, MatTable} from '@angular/material';
import {AlertService} from '../alert.service';
import {ConfirmationDialogService} from '../confirmation-dialog/confirmation-dialog.service';
import {fromEvent} from 'rxjs';
import {debounceTime, distinctUntilChanged, filter, tap} from 'rxjs/operators';
import {MissionsService} from '../api/services/missions.service';
import {Mission} from '../api/models/mission';
import {MissionsDatasource} from '../missions.datasource';
import {MissionViewModel} from '../models/mission-view-model';
import {MissionDialogComponent} from '../mission-dialog/mission-dialog.component';

@Component({
  selector: 'app-digital-goods-list',
  templateUrl: './missions-list.component.html',
  styleUrls: ['./missions-list.component.css']
})
export class MissionsListComponent implements OnInit, AfterViewInit {
  hasSelection = false;
  selection: SelectionModel<Mission>;
  dataSource: MissionsDatasource;
  displayedColumns = ['select', 'id', 'name', 'actions'];
  currentMissions: Mission[];

  @ViewChild(MatPaginator) paginator: MatPaginator;
  @ViewChild('input') input: ElementRef;
  @ViewChild(MatTable) table: MatTable<Mission>;

  constructor(private missionsService: MissionsService, private alertService: AlertService, private dialogService: ConfirmationDialogService, public dialog: MatDialog) { }

  ngOnInit() {
    this.selection = new SelectionModel<Mission>(true, []);
    this.dataSource = new MissionsDatasource(this.missionsService);
    this.paginator.pageSize = 10;
    this.refresh(0);
  }

  ngAfterViewInit() {
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

    this.selection.onChange.subscribe(s => this.hasSelection = this.selection.hasValue());
    this.dataSource.missions$.subscribe(currentMissions => this.currentMissions = currentMissions);
    this.dataSource.totalCount$.subscribe(totalCount => this.paginator.length = totalCount);
  }

  refresh(delay = 500) {
    setTimeout(() => {
      this.selection.clear();
      this.dataSource.loadMissions(
        this.input.nativeElement.value,
        this.paginator.pageIndex * this.paginator.pageSize,
        this.paginator.pageSize);
    }, delay)
  }

  /** Whether the number of selected elements matches the total number of rows. */
  isAllSelected() {
    const numSelected = this.selection.selected.length;
    const numRows = this.currentMissions.length;
    return numSelected == numRows;
  }

  /** Selects all rows if they are not all selected; otherwise clear selection. */
  masterToggle() {
    this.isAllSelected() ?
      this.selection.clear() :
      this.currentMissions.forEach(row => this.selection.select(row));
  }

  deleteMission(mission) {
    this.dialogService
      .confirm('Confirm Dialog', `Are you sure you want to delete the mission '${mission.name}'`)
      .pipe(filter(r => r))
      .subscribe(res => {
        this.doDeleteMission(mission);
        this.refresh();
      });
  }

  doDeleteMission(mission) {
    this.missionsService.deleteMission(mission.id).subscribe(r => {},
      error => this.alertService.error(error));
  }

  deleteSelectedMissions(){
    this.dialogService
      .confirm('Confirm Dialog', `Are you sure you want to delete the ${this.selection.selected.length} selected mission${this.selection.selected.length == 1 ? '' : 's'}?`)
      .pipe(filter(r => r))
      .subscribe(res => {
        this.selection.selected.forEach(row => this.doDeleteMission(row));
        this.selection.clear();
        this.refresh();
      });
  }

  showDialog(isNew: boolean, mission: Mission, next) {
    this.dialog.open(MissionDialogComponent, {
      width: '900px',
      data: { isNew: isNew, mission: mission, next: next, refresher: this }
    });
  }

  addMission() {
    this.showDialog(true, new MissionViewModel(), result => {
      return this.missionsService.createMission(result);
    });
  }

  editMission(mission) {
    this.showDialog(false, mission, res => {
      return this.missionsService.updateMission({ identifier: mission.id, body: res });
    });
  }
}
