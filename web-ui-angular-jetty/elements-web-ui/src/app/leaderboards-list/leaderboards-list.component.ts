import {AfterViewInit, Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {MatPaginator} from "@angular/material/paginator";
import {debounceTime, distinctUntilChanged, filter, map, tap} from "rxjs/operators";
import {fromEvent} from "rxjs";
import {SelectionModel} from "@angular/cdk/collections";
import {MatDialog} from "@angular/material/dialog";
import {MatTable} from '@angular/material/table'
import {AlertService} from "../alert.service";
import {ConfirmationDialogService} from "../confirmation-dialog/confirmation-dialog.service";

import { LeaderboardsService } from '../api/services';
import { Leaderboard } from '../api/models/leaderboard';
import { LeaderboardsDataSource } from '../leaderboards.datasource';
import { LeaderboardDialogComponent, ScoreStrategyType, TimeStrategyType } from '../leaderboard-dialog/leaderboard-dialog.component';
import { LeaderboardViewModel } from '../models/leaderboard-view-model';
import { DateTime} from 'luxon';

@Component({
  selector: 'app-leaderboards-list',
  templateUrl: './leaderboards-list.component.html',
  styleUrls: ['./leaderboards-list.component.css']
})
export class LeaderboardsListComponent implements OnInit, AfterViewInit {

  timeStrategyTypes: TimeStrategyType[] = [
    { key: "ALL_TIME", description: "All Time" },
    { key: "EPOCHAL", description: "Epochal" }
  ];

  scoreStrategyTypes: ScoreStrategyType[] = [
    { key: "OVERWRITE_IF_GREATER", description: "Overwrite" },
    { key: "ACCUMULATE", description: "Accumulate" }
  ];

  getStrategyDescription(key: string, strategyTypes: any[]): string {
    return strategyTypes.find(strategy => strategy.key === key).description;
  }

  hasSelection = false;
  selection: SelectionModel<Leaderboard>;
  dataSource: LeaderboardsDataSource;
  // TODO: "select" column was removed until we decide if we want to allow leaderboards to be deleted.
  //displayedColumns = ["select", "id", "name", "title", "scoreStrategyType", "timeStrategyType", "actions"];
  displayedColumns = ["id", "name", "title", "scoreStrategyType", "timeStrategyType", "actions"];
  currentLeaderboards: Leaderboard[];

  @ViewChild(MatPaginator) paginator: MatPaginator;
  @ViewChild('input') input: ElementRef;
  @ViewChild(MatTable) table: MatTable<Leaderboard>;

  constructor(
    private leaderboardsService: LeaderboardsService, 
    private alertService: AlertService, 
    private dialogService: ConfirmationDialogService, 
    public dialog: MatDialog
  ) { }

  ngOnInit() {
    this.selection = new SelectionModel<Leaderboard>(true, []);
    this.dataSource = new LeaderboardsDataSource(this.leaderboardsService);
    this.refresh(0);
  }

  ngAfterViewInit() {
    this.paginator.pageSize = 10;
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

    this.selection.changed.subscribe(s => this.hasSelection = this.selection.hasValue());
    this.dataSource.leaderboards$.subscribe(currentLeaderboards => this.currentLeaderboards = currentLeaderboards);
    this.dataSource.totalCount$.subscribe(totalCount => this.paginator.length = totalCount);
  }

    // add support for searching here
    refresh(delay = 500) {
      setTimeout(() => {
        this.selection.clear();
        this.dataSource.loadLeaderboards(
          this.input.nativeElement.value,
          this.paginator.pageIndex * this.paginator.pageSize,
          this.paginator.pageSize);
      }, delay);
    }

/** Whether the number of selected elements matches the total number of rows. */
isAllSelected() {
  const numSelected = this.selection.selected.length;
  const numRows = this.currentLeaderboards.length;
  return numSelected == numRows;
}

/** Selects all rows if they are not all selected; otherwise clear selection. */
masterToggle() {
  this.isAllSelected() ?
    this.selection.clear() :
    this.currentLeaderboards.forEach(row => this.selection.select(row));
}

deleteLeaderboard(leaderboard) {
  this.dialogService
    .confirm('Confirm Dialog', `Are you sure you want to delete the leaderboard '${leaderboard.name}'`)
    .pipe(filter(r => r))
    .subscribe(res => {
      this.doDeleteLeaderboard(leaderboard);
      this.refresh();
    });
}

// TODO: This follows the API and SHOULD WORK. Send error to backend devs. 
doDeleteLeaderboard(leaderboard) {
   this.leaderboardsService.deleteLeaderboard(leaderboard.id).subscribe(r => {},
     error => this.alertService.error(error));
}

deleteSelectedLeaderboards(){
  this.dialogService
    .confirm('Confirm Dialog', `Are you sure you want to delete the ${this.selection.selected.length} selected leaderboard${this.selection.selected.length==1 ? '' : 's'}?`)
    .pipe(filter(r => r))
    .subscribe(res => {
      this.selection.selected.forEach(row => this.doDeleteLeaderboard(row));
      this.selection.clear();
      this.refresh(500);
    });
}

showDialog(isNew: boolean, leaderboard: Leaderboard, next) {
  const leaderboardViewModel: LeaderboardViewModel = this.convertLBToLBViewModel(leaderboard);
  this.dialog.open(LeaderboardDialogComponent, {
    width: '800px',
    data: { isNew: isNew, leaderboard: leaderboardViewModel, next: next, refresher: this }
  });
}

convertLBToLBViewModel (leaderboard: Leaderboard): LeaderboardViewModel {
  const viewLeaderboard: LeaderboardViewModel = JSON.parse(JSON.stringify(leaderboard));

  const timeBreakdown = this.convertMS(leaderboard.epochInterval);
  viewLeaderboard.days = timeBreakdown.day;
  viewLeaderboard.hours = timeBreakdown.hour;
  viewLeaderboard.minutes = timeBreakdown.minute;
  viewLeaderboard.seconds = timeBreakdown.seconds;
  
  if(leaderboard.firstEpochTimestamp){
    viewLeaderboard.firstEpochTimestampView = 
    DateTime.fromMillis(leaderboard.firstEpochTimestamp).toString().split('.')[0];
  }

  return viewLeaderboard;
}

convertMS( milliseconds ) {
  var day, hour, minute, seconds;
  seconds = Math.floor(milliseconds / 1000);
  minute = Math.floor(seconds / 60);
  seconds = seconds % 60;
  hour = Math.floor(minute / 60);
  minute = minute % 60;
  day = Math.floor(hour / 24);
  hour = hour % 24;
  return {
      day: day,
      hour: hour,
      minute: minute,
      seconds: seconds
  };
}

 addLeaderboard() {
  this.showDialog(true, new LeaderboardViewModel(), result => {
    const leaderboard: Leaderboard = this.convertLBViewModeltoLB(result);
    return this.leaderboardsService.createLeaderboard(leaderboard);
  });
 }

 convertLBViewModeltoLB(leaderboardViewModel: LeaderboardViewModel): Leaderboard {
   
  const { firstEpochTimestampView, days, hours, minutes, seconds, name, timeStrategyType, scoreStrategyType, title, scoreUnits, zone } = leaderboardViewModel;
  let leaderboard: Leaderboard = {
    name,
    timeStrategyType,
    scoreStrategyType,
    title,
    scoreUnits,
    firstEpochTimestamp: 0,
    epochInterval: 0,
  };

  leaderboard.firstEpochTimestamp = DateTime.fromISO(firstEpochTimestampView, { zone }).toMillis();
  leaderboard.epochInterval = this.getTimeInMS(days, hours, minutes, seconds);

  if(leaderboard.timeStrategyType !== 'EPOCHAL'){
    delete leaderboard.firstEpochTimestamp;
    delete leaderboard.epochInterval;
  }
  
  return leaderboard;
 }

 getTimeInMS(days: number, hours: number, minutes: number, seconds: number) {
   const totalMS = ((((((days*24) + hours) * 60) + minutes) * 60 ) + seconds) * 1000; 
   return totalMS;
 }

 editLeaderboard(leaderboard) {
   this.showDialog(false, leaderboard, result => {
     const newLeaderboard = this.convertLBViewModeltoLB(result);
    return this.leaderboardsService.updateLeaderboard({nameOrId: leaderboard.id, body: newLeaderboard});
  });
 }    

}
