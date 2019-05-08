import {AfterViewInit, Component, ElementRef, Inject, OnInit, ViewChild} from '@angular/core';
import {User} from '../api/models/user';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef, MatPaginator, MatSnackBar, MatTable} from '@angular/material';
import {UsersService} from '../api/services/users.service';
import {AlertService} from '../alert.service';
import {UsersDataSource} from '../users.datasource';
import {fromEvent} from 'rxjs';
import {debounceTime, distinctUntilChanged, tap} from 'rxjs/operators';

@Component({
  selector: 'app-user-select-dialog',
  templateUrl: './user-select-dialog.component.html',
  styleUrls: ['./user-select-dialog.component.css']
})
export class UserSelectDialogComponent implements OnInit, AfterViewInit {
  dataSource: UsersDataSource;
  displayedColumns = ["id", "email", "level", "actions"];
  currentUsers: User[];

  @ViewChild(MatPaginator) paginator: MatPaginator;
  @ViewChild('input') input: ElementRef;
  @ViewChild(MatTable) table: MatTable<User>;

  constructor(public dialogRef: MatDialogRef<UserSelectDialogComponent>, public dialog: MatDialog, @Inject(MAT_DIALOG_DATA) public data: any,
              private usersService: UsersService, private alertService: AlertService, private snackBar: MatSnackBar) { }

  ngOnInit() {
    this.dataSource = new UsersDataSource(this.usersService);
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

    this.dataSource.users$.subscribe(currentUsers => this.currentUsers = currentUsers);
    this.dataSource.totalCount$.subscribe(totalCount => this.paginator.length = totalCount);
  }

  refresh(delay = 500) {
    setTimeout(() => {
      this.dataSource.loadUsers(
        this.input.nativeElement.value,
        this.paginator.pageIndex * this.paginator.pageSize,
        this.paginator.pageSize);
    }, delay);
  }

  close(user?: User) {
    if(!user) {
      this.dialogRef.close();
      return;
    }

    this.data.next(user);
    this.dialogRef.close();
  }

}
