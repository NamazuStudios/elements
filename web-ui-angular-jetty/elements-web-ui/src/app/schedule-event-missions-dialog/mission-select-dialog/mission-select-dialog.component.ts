import {AfterViewInit, Component, ElementRef, Inject, OnInit, ViewChild} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material/dialog';
import {MatPaginator} from '@angular/material/paginator';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatTable} from '@angular/material/table';
import {AlertService} from '../../alert.service';
import {fromEvent} from 'rxjs';
import {debounceTime, distinctUntilChanged, tap} from 'rxjs/operators';
import {Mission} from 'src/app/api/models';
import {MissionsService} from 'src/app/api/services';
import {MissionsDatasource} from "../../missions.datasource";

@Component({
  selector: 'app-missionselect-dialog',
  templateUrl: './mission-select-dialog.component.html',
  styleUrls: ['./mission-select-dialog.component.css']
})
export class MissionSelectDialogComponent implements OnInit, AfterViewInit {

  dataSource: MissionsDatasource;
  displayedColumns = ["name", "displayName", "actions"];
  currentMissions: Mission[];

  @ViewChild(MatPaginator) paginator: MatPaginator;
  @ViewChild('input') input: ElementRef;
  @ViewChild(MatTable) table: MatTable<Mission>;

  constructor(public dialogRef: MatDialogRef<MissionSelectDialogComponent>,
              public dialog: MatDialog,
              @Inject(MAT_DIALOG_DATA) public data: any,
              private missionsService: MissionsService,
              private alertService: AlertService,
              private snackBar: MatSnackBar) { }

  ngOnInit() {
    this.dataSource = new MissionsDatasource(this.missionsService);
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

    this.dataSource.missions$.subscribe(currentMissions => this.currentMissions = currentMissions);
    this.dataSource.totalCount$.subscribe(totalCount => this.paginator.length = totalCount);
  }

  refresh(delay = 500) {
    setTimeout(() => {
      this.dataSource.loadMissions(
        this.input.nativeElement.value,
        this.paginator.pageIndex * this.paginator.pageSize,
        this.paginator.pageSize);
    }, delay);
  }

  close(mission?: Mission) {
    if(!mission) {
      this.dialogRef.close();
      return;
    }

    this.data.next(mission);
    this.dialogRef.close();
  }

}









