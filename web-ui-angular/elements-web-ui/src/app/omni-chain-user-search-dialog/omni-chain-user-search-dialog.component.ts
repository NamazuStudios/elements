import { Component, Inject, OnInit, ViewChild } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatPaginator } from '@angular/material/paginator';
import { tap } from 'rxjs/operators';
import { User } from '../api/models';
import { UsersService } from '../api/services';
import { UsersDataSource } from '../users.datasource';

interface Data {
  onSelect: (user: User) => void;
}

@Component({
  selector: 'app-omni-chain-vaults-user-search-dialog',
  templateUrl: './omni-chain-user-search-dialog.component.html',
  styleUrls: ['./omni-chain-user-search-dialog.component.css']
})
export class OmniChainUserSearchDialogComponent implements OnInit {

  @ViewChild(MatPaginator) paginator: MatPaginator;
  displayedColumns: string[] = [
    'id',
    'name',
    'email',
    'selectAction',
  ];
  dataSource: UsersDataSource;
  users: User[] = [];
  searchValue = '';

  constructor(
    public dialogRef: MatDialogRef<OmniChainUserSearchDialogComponent>,
    private usersService: UsersService,
    @Inject(MAT_DIALOG_DATA) public data: Data,
  ) { }

  ngOnInit(): void {
    this.dataSource = new UsersDataSource(this.usersService);
    this.refresh();
  }

  ngAfterViewInit() {
    this.paginator.page
      .pipe(
        tap(() => this.refresh())
      )
      .subscribe();

    this.dataSource.users$.subscribe(users => this.users = users);
    this.dataSource.totalCount$.subscribe(totalCount => this.paginator.length = totalCount);
  }

  search(value: string) {
    this.searchValue = value;
    this.refresh();
  }

  refresh(delay = 0) {
    setTimeout(() => {
      this.dataSource.loadUsers(
        this.searchValue,
        this.paginator.pageIndex * this.paginator.pageSize,
        this.paginator.pageSize);
    }, delay);
  }

  onSelect(user: User) {
    this.data.onSelect(user);
    this.close();
  }

  close() {
    this.dialogRef.close();
  }
}
