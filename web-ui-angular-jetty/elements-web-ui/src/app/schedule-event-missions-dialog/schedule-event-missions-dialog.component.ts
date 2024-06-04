import {Component, ElementRef, Inject, OnInit, ViewChild} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material/dialog';
import {MatTable} from "@angular/material/table";
import {Mission} from "../api/models/mission";
import {MissionSelectDialogComponent} from "./mission-select-dialog/mission-select-dialog.component";

@Component({
  selector: 'schedule-event-missions-dialog',
  templateUrl: './schedule-event-missions-dialog.component.html',
  styleUrls: ['./schedule-event-missions-dialog.component.css']
})
export class ScheduleEventMissionsDialogComponent implements OnInit {

  hasSelection = false;
  displayedColumns = ['name', 'delete-action'];
  currentMissionNames: string[];

  @ViewChild('input') input: ElementRef;
  @ViewChild(MatTable) table: MatTable<Mission>;

  constructor(
    public dialog: MatDialog,
    @Inject(MAT_DIALOG_DATA) public data: any,
    public dialogRef: MatDialogRef<ScheduleEventMissionsDialogComponent>
  ) { }

  ngOnInit() {
    this.currentMissionNames = this.data.missions;
  }

  close(saveChanges?: boolean): void {
    if (!saveChanges) {
      this.dialogRef.close();
      return;
    }

    this.data.next(this.currentMissionNames);
    this.dialogRef.close();
  }

  removeMission(name) {
    this.currentMissionNames = this.currentMissionNames.filter(mission => mission !== name);
  }

  addMission() {
    this.dialog.open(MissionSelectDialogComponent, {
      width: '500px',
      data: {
        next: result => {
          this.currentMissionNames = [...this.currentMissionNames, result.name];
        }
      }
    });
  }

  missionsSet() {
    return this.currentMissionNames && this.currentMissionNames.length > 0;
  }
}
