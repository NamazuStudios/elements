import {AfterViewInit, Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {SelectionModel} from '@angular/cdk/collections';
import {MatDialog} from '@angular/material/dialog';
import {MatPaginator} from '@angular/material/paginator';
import {MatTable} from '@angular/material/table';
import {AlertService} from '../alert.service';
import {ConfirmationDialogService} from '../confirmation-dialog/confirmation-dialog.service';
import {fromEvent} from 'rxjs';
import {debounceTime, distinctUntilChanged, filter, tap} from 'rxjs/operators';
import {SchedulesDatasource} from "../api/schedule.datasource";
import {Schedule} from "../api/models/schedule";
import {SchedulesService} from "../api/services/schedules.service";
import {ScheduleDialogComponent} from "../schedule-dialog/schedule-dialog.component";
import {ScheduleViewModel} from "../models/schedule-view-model";

@Component({
  selector: 'app-digital-goods-list',
  templateUrl: './schedules-list.component.html',
  styleUrls: ['./schedules-list.component.css']
})
export class SchedulesListComponent implements OnInit, AfterViewInit {
  hasSelection = false;
  selection: SelectionModel<Schedule>;
  dataSource: SchedulesDatasource;
  displayedColumns = ['select', 'id', 'name', 'edit-action', 'delete-action', 'events-action'];
  currentSchedules: Schedule[];

  @ViewChild(MatPaginator) paginator: MatPaginator;
  @ViewChild('input') input: ElementRef;
  @ViewChild(MatTable) table: MatTable<Schedule>;

  constructor(private schedulesService: SchedulesService, private alertService: AlertService,
              private dialogService: ConfirmationDialogService, public dialog: MatDialog) { }

  ngOnInit() {
    this.selection = new SelectionModel<Schedule>(true, []);
    this.dataSource = new SchedulesDatasource(this.schedulesService);
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

    this.selection.changed.subscribe(() => this.hasSelection = this.selection.hasValue());
    this.dataSource.schedules$.subscribe(currentSchedules => this.currentSchedules = currentSchedules);
    this.dataSource.totalCount$.subscribe(totalCount => this.paginator.length = totalCount);
  }

  refresh(delay = 500) {
    setTimeout(() => {
      this.selection.clear();
      this.dataSource.loadSchedules(
        this.input.nativeElement.value,
        this.paginator.pageIndex * this.paginator.pageSize,
        this.paginator.pageSize);
    }, delay)
  }

  /** Whether the number of selected elements matches the total number of rows. */
  isAllSelected() {
    const numSelected = this.selection.selected.length;
    const numRows = this.currentSchedules.length;
    return numSelected == numRows;
  }

  /** Selects all rows if they are not all selected; otherwise clear selection. */
  masterToggle() {
    this.isAllSelected() ?
      this.selection.clear() :
      this.currentSchedules.forEach(row => this.selection.select(row));
  }

  deleteSchedule(schedule) {
    this.dialogService
      .confirm('Confirm Dialog', `Are you sure you want to delete this schedule '${schedule.name}'`)
      .pipe(filter(r => r))
      .subscribe(() => {
        this.doDeleteSchedule(schedule);
        this.refresh();
      });
  }

  doDeleteSchedule(schedule) {
    this.schedulesService.deleteSchedule(schedule.id).subscribe(() => {},
      error => this.alertService.error(error));
  }

  deleteSelectedSchedules(){
    this.dialogService
      .confirm('Confirm Dialog', `Are you sure you want to delete the ${this.selection.selected.length} selected schedule${this.selection.selected.length == 1 ? '' : 's'}?`)
      .pipe(filter(r => r))
      .subscribe(() => {
        this.selection.selected.forEach(row => this.doDeleteSchedule(row));
        this.selection.clear();
        this.refresh();
      });
  }

  showDialog(isNew: boolean, schedule: Schedule, next) {
    this.dialog.open(ScheduleDialogComponent, {
      width: '900px',
      data: { isNew: isNew, schedule: schedule, next: next, refresher: this }
    });
  }

  addSchedule() {
    this.showDialog(true, new ScheduleViewModel(), result => {
      return this.schedulesService.createSchedule(result);
    });
  }

  editSchedule(schedule) {
    this.showDialog(false, schedule, res => {
      return this.schedulesService.updateSchedule({ identifier: schedule.id, body: res });
    });
  }

  editEvents(schedule) {
    console.log(schedule)
  }
}
