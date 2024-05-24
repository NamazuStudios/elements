import {AfterViewInit, Component, ElementRef, Inject, OnInit, ViewChild} from '@angular/core';
import {FormBuilder} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material/dialog';
import {ScheduleEventsService} from "../api/services/schedule-events.service";
import {CreateScheduleEventRequest} from "../api/models/create-schedule-event-request";
import {AlertService} from "../alert.service";
import {MatPaginator} from "@angular/material/paginator";
import {MatTable} from "@angular/material/table";
import {ScheduleEventsDatasource} from "../schedule.events.datasource";
import {filter, tap} from "rxjs/operators";
import {ScheduleEvent} from "../api/models/schedule-event";
import {ConfirmationDialogService} from "../confirmation-dialog/confirmation-dialog.service";
import {
  ScheduleEventMissionsDialogComponent
} from "../schedule-event-missions-dialog/schedule-event-missions-dialog.component";
import {UpdateScheduleEventRequest} from "../api/models/update-schedule-event-request";

@Component({
  selector: 'schedule-events-dialog',
  templateUrl: './schedule-events-dialog.component.html',
  styleUrls: ['./schedule-events-dialog.component.css']
})
export class ScheduleEventsDialogComponent implements OnInit, AfterViewInit {

  hasSelection = false;
  scheduleEventsDatasource: ScheduleEventsDatasource;
  displayedColumns = ['id', 'begin', 'end', 'delete-action', 'missions-action'];
  currentScheduleEvents: ScheduleEvent[];
  newMissionNames: string[]

  @ViewChild(MatPaginator) paginator: MatPaginator;
  @ViewChild('input') input: ElementRef;
  @ViewChild(MatTable) table: MatTable<ScheduleEvent>;

  scheduleEventForm = this.formBuilder.group({
    begin: [],
    end: [],
    scheduleId: [{value: this.data.schedule.id, disabled: true}],
  });

  constructor(
    private scheduleEventsService: ScheduleEventsService, private dialogService: ConfirmationDialogService,
    private alertService: AlertService, public dialog: MatDialog,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private formBuilder: FormBuilder,
    public dialogRef: MatDialogRef<ScheduleEventsDialogComponent>
  ) { }

  ngOnInit() {
    this.scheduleEventsDatasource = new ScheduleEventsDatasource(this.scheduleEventsService);
    this.refresh(0);
  }

  ngAfterViewInit() {
    this.paginator.pageSize = 10;

    this.paginator.page
      .pipe(
        tap(() => this.refresh())
      )
      .subscribe();

    this.scheduleEventsDatasource.scheduleEvents$.subscribe(scheduleEvents => this.currentScheduleEvents = scheduleEvents);
    this.scheduleEventsDatasource.totalCount$.subscribe(totalCount => this.paginator.length = totalCount);
  }

  close(saveChanges?: boolean): void {
    if (!saveChanges) {
      this.dialogRef.close();
      return;
    }
  }

  addScheduleEvent() {
    const formData = this.scheduleEventForm.value;
    let createEventRequest: CreateScheduleEventRequest = {
      begin: this.convertToTimestamp(formData.begin),
      end: this.convertToTimestamp(formData.end),
      missionNamesOrIds: this.newMissionNames
    }

    this.scheduleEventsService.createScheduleEvent(createEventRequest, this.data.schedule.id).subscribe(() => {
        this.afterMissionsSetup("New event added.")
      },
        err => {
      this.alertService.error(err);
    });
  }

  updateScheduleEvent(updatedScheduleEvent: ScheduleEvent, updatedMissionNames?: string[]) {
    let updateRequest: UpdateScheduleEventRequest = {
      begin: updatedScheduleEvent.begin,
      end: updatedScheduleEvent.end,
      missionNamesOrIds: updatedMissionNames
    }
    this.scheduleEventsService.updateScheduleEvent(updateRequest, this.data.schedule.id, updatedScheduleEvent.id).subscribe(() => {
        this.afterMissionsSetup("Event updated.")
      },
      err => {
        this.alertService.error(err);
      });
  }

  refresh(delay = 500) {
    setTimeout(() => {
      this.scheduleEventsDatasource.loadScheduleEvents(
        this.data.schedule.id,
        this.paginator.pageIndex * this.paginator.pageSize,
        this.paginator.pageSize);
    }, delay)
  }

  deleteScheduleEvent(scheduleEvent) {
    this.dialogService
      .confirm('Confirm Dialog', `Are you sure you want to delete this event`)
      .pipe(filter(r => r))
      .subscribe(() => {
        this.doDeleteScheduleEvent(scheduleEvent);
        this.refresh();
      });
  }

  doDeleteScheduleEvent(scheduleEvent) {
    this.scheduleEventsService.deleteScheduleEvent(this.data.schedule.id, scheduleEvent.id).subscribe(() => {},
      error => this.alertService.error(error));
  }

  convertToTimestamp(date: string) {
    return new Date(date).getTime();
  }

  convertToDate(timestamp: number) {
    if (timestamp == 0) {
      return 'not set'
    }
    const date = new Date(timestamp);
    let day = String(date.getDate()).padStart(2, '0');
    let month = String(date.getMonth() + 1).padStart(2, '0'); // January is 0
    let year = date.getFullYear();

    return month + '/' + day + '/' + year;
  }

  showMissionsDialog(isNew: boolean, scheduleEvent?: ScheduleEvent) {
    let currentMissionNames = this.newEventMissionsSet() ? this.newMissionNames : [];
    if (!isNew && scheduleEvent.missions) {
      currentMissionNames = scheduleEvent.missions.map(mission => mission.name);
    }
    this.dialog.open(ScheduleEventMissionsDialogComponent, {
      width: '650px',
      data: { isNew: isNew, missions: currentMissionNames, refresher: this,
        next: result => {
          if(isNew) {
            this.newMissionNames = result;
          } else {
            this.updateScheduleEvent(scheduleEvent, result);
          }
        }
      }
    });
  }

  newEventMissionsSet(): boolean {
    return this.newMissionNames && this.newMissionNames.length > 0;
  }

  afterMissionsSetup(resultMessage: string) {
    this.refresh(0);
    this.scheduleEventForm.reset();
    this.newMissionNames = [];
    this.alertService.success(resultMessage)
  }
}
