import {AfterViewInit, Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar'
import {FormBuilder, Validators} from "@angular/forms";
import {AlertService} from '../alert.service';
import { LeaderboardsService } from '../api/services';
import { LeaderboardExistsValidator } from '../leaderboard-exists-validator';

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

// TODO... 1) make sure data from last two items in group get sent in as MS....
// TODO... 2) make sure leaderboard exists validator implemented
// TODO... 3) make sure time zone picker is available for new letterboards

  leaderboardForm = this.formBuilder.group({
    //name: [ this.data.leaderboard.name, [ Validators.required ], [this.leaderboardExistsValidator.validate]], 
    // TODO: make sure the above line is uncommented so that we make sure that the name is unique for NEW items addeed...
    name: [ this.data.leaderboard.name, [ Validators.required ]], 
    title: [ this.data.leaderboard.title, [ Validators.required ]], 
    timeStrategyType: [ this.data.leaderboard.timeStrategyType, [ Validators.required ]],
    scoreStrategyType: [ this.data.leaderboard.scoreStrategyType, [ Validators.required ]], 
    scoreUnits: [ this.data.leaderboard.scoreUnits, [ Validators.required ]], 

    firstEpochTimestampView: [ {value: this.data.leaderboard.firstEpochTimestampView, disabled: !this.data.isNew}], // this must be converted into ms by getting current time (Date.getTime()) and adding the difference    
    days: [ {value: this.data.leaderboard.days, disabled: !this.data.isNew}],
    hours: [ {value: this.data.leaderboard.hours, disabled: !this.data.isNew}],
    minutes: [ {value: this.data.leaderboard.minutes, disabled: !this.data.isNew}],
    seconds: [ {value: this.data.leaderboard.seconds, disabled: !this.data.isNew}]

    // make sure to unccommend this and fix the validation
    // firstEpochTimestampView: [ this.data.leaderboard.firstEpochTimestampView, [ Validators.required ]], // this must be converted into ms by getting current time (Date.getTime()) and adding the difference    
    // days: [ this.data.leaderboard.days, [ Validators.required ]],
    // hours: [ this.data.leaderboard.hours, [ Validators.required ]],
    // minutes: [ this.data.leaderboard.minutes, [ Validators.required ]],
    // seconds: [ this.data.leaderboard.seconds, [ Validators.required ]]
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

  getCurrentOffset() {
    const offset = (new Date).getTimezoneOffset()/60;
    return `UTC ${offset > 0 ? '+' : '-'} ${ Math.abs(offset)}`;
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

