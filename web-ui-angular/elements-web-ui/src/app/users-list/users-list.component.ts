import {AfterViewInit, ChangeDetectorRef, Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {UsersService} from "../api/services/users.service";
import {UsersDataSource} from "../users.datasource";
import {MatPaginator} from "@angular/material/paginator";
import {debounceTime, distinctUntilChanged, filter, tap} from "rxjs/operators";
import {BehaviorSubject, fromEvent} from "rxjs";
import {SelectionModel} from "@angular/cdk/collections";
import {User} from "../api/models/user";
import {MatTable} from "@angular/material";
import {AlertService} from "../alert.service";
import {ConfirmationDialogService} from "../confirmation-dialog/confirmation-dialog.service";

@Component({
  selector: 'app-users-list',
  templateUrl: './users-list.component.html',
  styleUrls: ['./users-list.component.css']
})
export class UsersListComponent implements OnInit, AfterViewInit {
  hasSelection = false;
  selection: SelectionModel<User>;
  dataSource: UsersDataSource;
  displayedColumns= ["select", "id", "email", "level", "actions"];
  currentUsers: User[];

  @ViewChild(MatPaginator) paginator: MatPaginator;
  @ViewChild('input') input: ElementRef;
  @ViewChild(MatTable) table: MatTable<User>;

  constructor(private usersService: UsersService, private alertService: AlertService, private dialogService: ConfirmationDialogService) { }

  ngOnInit() {
    this.selection = new SelectionModel<User>(true, []);

    this.dataSource = new UsersDataSource(this.usersService);
    this.paginator.pageSize = 10;
    this.refresh();
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

    this.selection.onChange.subscribe(s => this.hasSelection = this.selection.hasValue());
    this.dataSource.users$.subscribe(currentUsers => this.currentUsers = currentUsers);
    this.dataSource.totalCount$.subscribe(totalCount => this.paginator.length = totalCount);
  }

  // add support for searching here
  refresh() {
    this.selection.clear();
    this.dataSource.loadUsers(
      this.input.nativeElement.value,
      this.paginator.pageIndex,
      this.paginator.pageSize);
  }

  /** Whether the number of selected elements matches the total number of rows. */
  isAllSelected() {
    const numSelected = this.selection.selected.length;
    const numRows = this.currentUsers.length;
    return numSelected == numRows;
  }

  /** Selects all rows if they are not all selected; otherwise clear selection. */
  masterToggle() {
    this.isAllSelected() ?
      this.selection.clear() :
      this.currentUsers.forEach(row => this.selection.select(row));
  }

  editUser(user) {
    console.log('edit');
    console.log(user);
    this.refresh();
  }

  deleteUser(user) {
    this.dialogService
      .confirm('Confirm Dialog', `Are you sure you want to delete the user '${user.name}'`)
      .pipe(filter(r => r))
      .subscribe(res => {
        this.doDeleteUser(user);
        setTimeout(() => this.refresh(), 500);
      });
  }

  doDeleteUser(user) {
    this.usersService.deactivateUser(user.id).subscribe(r => {},
      error => this.alertService.error(error));
  }

  addUser() {
    console.log('add');
    this.refresh();
  }

  deleteSelectedUsers() {
    this.dialogService
      .confirm('Confirm Dialog', `Are you sure you want to delete the ${this.selection.selected.length} selected user${this.selection.selected.length==1 ? '' : 's'}?`)
      .pipe(filter(r => r))
      .subscribe(res => {
        this.selection.selected.forEach(row => this.doDeleteUser(row));
        this.selection.clear();
        setTimeout(() => this.refresh(), 500);
      });
  }
}
