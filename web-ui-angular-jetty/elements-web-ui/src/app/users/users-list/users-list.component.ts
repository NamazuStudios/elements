import {AfterViewInit, Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {UsersService} from "../../api/services/users.service";
import {UsersDataSource} from "../users.datasource";
import {MatPaginator} from "@angular/material/paginator";
import {debounceTime, distinctUntilChanged, filter, tap} from "rxjs/operators";
import {fromEvent} from "rxjs";
import {SelectionModel} from "@angular/cdk/collections";
import {User} from "../../api/models/user";
import {MatDialog} from "@angular/material/dialog";
import {MatTable} from '@angular/material/table'
import {AlertService} from "../../alert.service";
import {ConfirmationDialogService} from "../../confirmation-dialog/confirmation-dialog.service";
import {UserDialogComponent} from "../user-dialog/user-dialog.component";
import {UserViewModel} from "../../models/user-view-model";
import {InventoryDialogComponent} from '../../digital-goods/inventory-dialog/inventory-dialog.component';

@Component({
  selector: 'app-users-list',
  templateUrl: './users-list.component.html',
  styleUrls: ['./users-list.component.css']
})
export class UsersListComponent implements OnInit, AfterViewInit {
  hasSelection = false;
  selection: SelectionModel<User>;
  dataSource: UsersDataSource;
  displayedColumns = ["select", "id", "name", "email", "linkedAccounts", "level", "inventory-action", "edit-action", "delete-action"];
  currentUsers: User[];

  @ViewChild(MatPaginator) paginator: MatPaginator;
  @ViewChild('input') input: ElementRef;
  @ViewChild(MatTable) table: MatTable<User>;

  constructor(private usersService: UsersService, private alertService: AlertService, private dialogService: ConfirmationDialogService, public dialog: MatDialog) { }

  ngOnInit() {
    this.selection = new SelectionModel<User>(true, []);
    this.dataSource = new UsersDataSource(this.usersService);
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
    this.dataSource.users$.subscribe(currentUsers => this.currentUsers = currentUsers);
    this.dataSource.totalCount$.subscribe(totalCount => this.paginator.length = totalCount);
  }

  // add support for searching here
  refresh(delay = 500) {
    setTimeout(() => {
      this.selection.clear();
      this.dataSource.loadUsers(
        this.input.nativeElement.value,
        this.paginator.pageIndex * this.paginator.pageSize,
        this.paginator.pageSize);
    }, delay);
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

  deleteUser(user) {
    this.dialogService
      .confirm('Confirm Dialog', `Are you sure you want to delete the user '${user.name}'`)
      .pipe(filter(r => r))
      .subscribe(res => {
        this.doDeleteUser(user);
        this.refresh();
      });
  }

  doDeleteUser(user) {
    this.usersService.deactivateUser(user.id).subscribe(r => {},
      error => this.alertService.error(error));
  }

  deleteSelectedUsers(){
    this.dialogService
      .confirm('Confirm Dialog', `Are you sure you want to delete the ${this.selection.selected.length} selected user${this.selection.selected.length==1 ? '' : 's'}?`)
      .pipe(filter(r => r))
      .subscribe(res => {
        this.selection.selected.forEach(row => this.doDeleteUser(row));
        this.selection.clear();
        this.refresh(500);
      });
  }

  showDialog(isNew: boolean, user: User, next) {
    this.dialog.open(UserDialogComponent, {
      width: '500px',
      data: { isNew: isNew, user: user, next: next, refresher: this }
    });
  }

  addUser() {
    this.showDialog(true, new UserViewModel(), result => {
      // backend expects password to be in query params, so delete from result before attaching to body
      const password = result.password;
      delete result.passwordConfirmation;
      //delete result.password;
      return this.usersService.createUser({ password: password, body: result });
    });
  }

  editUser(user) {
    this.showDialog(false, user, result => {
      delete result.passwordConfirmation;
      const id = result.id;
      delete result.id;
      if(result.password === "") { delete result.password }
      return this.usersService.updateUser({name: id, body: result});
    });
  }

  editInventory(user) {
    this.dialog.open(InventoryDialogComponent, {
      width: '1000px',
      data: {
        user: user
      }
    });
  }

  getLinkedAccountsForUser(user: User) {
    return user.linkedAccounts?.map(item => {
      const parts = item.split('.');
      const lastPart = parts[parts.length - 1];
      return lastPart.charAt(0).toUpperCase() + lastPart.slice(1);
    })
  }
}
