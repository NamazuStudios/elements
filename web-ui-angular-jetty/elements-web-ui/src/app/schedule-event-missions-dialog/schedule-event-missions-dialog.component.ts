import {Component, ElementRef, Inject, OnInit, ViewChild} from '@angular/core';
import {FormBuilder} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {AlertService} from "../alert.service";
import {MatTable} from "@angular/material/table";
import {ConfirmationDialogService} from "../confirmation-dialog/confirmation-dialog.service";
import {Mission} from "../api/models/mission";

@Component({
  selector: 'schedule-event-missions-dialog',
  templateUrl: './schedule-event-missions-dialog.component.html',
  styleUrls: ['./schedule-event-missions-dialog.component.css']
})
export class ScheduleEventMissionsDialogComponent implements OnInit {

  hasSelection = false;
  displayedColumns = ['name', 'delete-action'];
  currentMissions: string[];

  @ViewChild('input') input: ElementRef;
  @ViewChild(MatTable) table: MatTable<Mission>;

  missionsForm = this.formBuilder.group({
    name: []
  });

  constructor(
    private dialogService: ConfirmationDialogService,
    private alertService: AlertService,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private formBuilder: FormBuilder,
    public dialogRef: MatDialogRef<ScheduleEventMissionsDialogComponent>
  ) { }

  ngOnInit() {
    this.currentMissions = this.data.missions;
  }

  close(saveChanges?: boolean): void {
    if (!saveChanges) {
      this.dialogRef.close();
      return;
    }
  }

  removeMission(name) {
    this.currentMissions = this.currentMissions.filter(mission => mission !== name);
  }

  addMission() {
    let newMission = this.missionsForm.value.name;
    this.currentMissions = [...this.currentMissions, newMission];
    this.missionsForm.reset();
  }
}
