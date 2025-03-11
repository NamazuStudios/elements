import {AfterViewInit, Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar'
import {AbstractControl, FormBuilder, Validators} from "@angular/forms";
import {AlertService} from '../../alert.service';
import { LeaderboardsService } from '../../api/services';
import { LeaderboardExistsValidator } from '../leaderboard-exists-validator';
import { getTimeZones, TimeZone } from "@vvo/tzdb";
import { DateTime } from 'luxon';

export interface TimeStrategyType {
  key: string;
  description: string;
}

export interface ScoreStrategyType {
  key: string;
  description: string;
}

@Component({
  selector: 'app-leaderboard-dialog',
  templateUrl: './leaderboard-dialog.component.html',
  styleUrls: ['./leaderboard-dialog.component.css']
})
export class LeaderboardDialogComponent implements OnInit, AfterViewInit {

  timeStrategyTypes: TimeStrategyType[] = [
    { key: "ALL_TIME", description: "All Time" },
    { key: "EPOCHAL", description: "Epochal" }
  ];

  scoreStrategyTypes: ScoreStrategyType[] = [
    { key: "OVERWRITE_IF_GREATER", description: "Overwrite if greater" },
    { key: "ACCUMULATE", description: "Accumulate" }
  ];

  private leaderboardExistsValidator = new LeaderboardExistsValidator(this.leaderboardsService);
  public timeZone!: string;
  public timeZones: TimeZone[];

// TODO... make sure leaderboard exists validator implemented

  leaderboardForm = this.formBuilder.group({
    //name: [ this.data.leaderboard.name, [ Validators.required ], [this.leaderboardExistsValidator.validate]],
    // TODO: make sure the above line is uncommented so that we make sure that the name is unique for NEW items addeed...
    name: [ this.data.leaderboard.name, [ Validators.required ]],
    title: [ this.data.leaderboard.title, [ Validators.required ]],
    timeStrategyType: [ this.data.leaderboard.timeStrategyType, [ Validators.required ]],
    scoreStrategyType: [ this.data.leaderboard.scoreStrategyType, [ Validators.required ]],
    scoreUnits: [ this.data.leaderboard.scoreUnits, [ Validators.required ]],

    zone: [ {value: DateTime.local().zoneName, disabled: !this.data.isNew}],
    firstEpochTimestampView: [ {value: this.data.leaderboard.firstEpochTimestampView, disabled: !this.data.isNew}],
    days: [ {value: this.data.leaderboard.days || 0, disabled: !this.data.isNew}],
    hours: [ {value: this.data.leaderboard.hours || 0, disabled: !this.data.isNew}],
    minutes: [ {value: this.data.leaderboard.minutes || 0, disabled: !this.data.isNew}],
    seconds: [ {value: this.data.leaderboard.seconds || 0, disabled: !this.data.isNew}]

  });

  constructor(public dialogRef: MatDialogRef<LeaderboardDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any,
              private formBuilder: FormBuilder,
              private alertService: AlertService,
              private leaderboardsService: LeaderboardsService,
              private snackBar: MatSnackBar) { }

  ngOnInit() {
    this.alertService.getMessage().subscribe((message: any) => {
      if(message) {
        this.snackBar.open(message.text, "Dismiss", { duration: 3000 });
      }
    });
    this.getTimeZoneData();
    //Listen to time strategy type value and update validators of reset date/time/period accordingly
    this.leaderboardForm.get('timeStrategyType').valueChanges.subscribe(data => this.onTimeStrategyChanged(data));
  }

  onTimeStrategyChanged(value: any){

    let controlsNames = [ 'firstEpochTimestampView', 'days', 'hours', 'minutes', 'seconds'];
    let controls: AbstractControl[] = controlsNames.map( (value) => this.leaderboardForm.get(value) );

    // Using setValidators to add and remove validators. No better support for adding and removing validators to controller atm.
    // See issue: https://github.com/angular/angular/issues/10567
    if(value==="EPOCHAL"){
      controls.forEach( control => control.setValidators([Validators.required]));
    }else {
      controls.forEach( control => control.setValidators([]));
    }
    controls.forEach( control => control.updateValueAndValidity()); //Need to call this to trigger a update
}

  ngAfterViewInit() {
    this.timeZone = Intl.DateTimeFormat().resolvedOptions().timeZone;
  }

  get timeStrategyType() {
    return this.leaderboardForm.get('timeStrategyType').value;
  }

  showEpochalOption() {
    return this.timeStrategyType === 'EPOCHAL';
  }

  getTimeZoneData() {
    const zones: TimeZone[] = getTimeZones();
    const luxonValidTimezones = zones.filter(tz => tz.name.includes('/') && DateTime.local().setZone(tz.name).isValid);
    this.timeZones = luxonValidTimezones;
  }

  close(res?: any) {
    if (!res) {
      this.dialogRef.close();
      return;
    }

    this.data.next(res).subscribe(r => {
      this.dialogRef.close();
      if (this.data.refresher) {
        this.data.refresher.refresh();
      }
    }, err => {
      this.alertService.error(err);
    });
  }
}

