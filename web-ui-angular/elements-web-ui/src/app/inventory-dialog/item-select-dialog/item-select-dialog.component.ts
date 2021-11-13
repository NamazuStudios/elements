import {AfterViewInit, Component, ElementRef, Inject, OnInit, ViewChild} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material/dialog';
import {MatPaginator} from '@angular/material/paginator';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatTable} from '@angular/material/table';
import {AlertService} from '../../alert.service';
import {fromEvent} from 'rxjs';
import {debounceTime, distinctUntilChanged, tap} from 'rxjs/operators';
import { Item } from 'src/app/api/models';
import { ItemsService } from 'src/app/api/services';
import { ItemsDataSource } from 'src/app/items.datasource';

@Component({
  selector: 'app-item-select-dialog',
  templateUrl: './item-select-dialog.component.html',
  styleUrls: ['./item-select-dialog.component.css']
})
export class ItemSelectDialogComponent implements OnInit, AfterViewInit {
  dataSource: ItemsDataSource;
  displayedColumns = ["name", "actions"];
  currentItems: Item[];

  @ViewChild(MatPaginator) paginator: MatPaginator;
  @ViewChild('input') input: ElementRef;
  @ViewChild(MatTable) table: MatTable<Item>;

  constructor(public dialogRef: MatDialogRef<ItemSelectDialogComponent>, public dialog: MatDialog, @Inject(MAT_DIALOG_DATA) public data: any,
              private itemsService: ItemsService, private alertService: AlertService, private snackBar: MatSnackBar) { }

  ngOnInit() {
    this.dataSource = new ItemsDataSource(this.itemsService);
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

    this.dataSource.items$.subscribe(currentItems => this.currentItems = currentItems);
    this.dataSource.totalCount$.subscribe(totalCount => this.paginator.length = totalCount);
  }

  refresh(delay = 500) {
    setTimeout(() => {
      this.dataSource.loadItems(
        this.input.nativeElement.value,
        this.paginator.pageIndex * this.paginator.pageSize,
        this.paginator.pageSize);
    }, delay);
  }

  close(item?: Item) {
    if(!item) {
      this.dialogRef.close();
      return;
    }

    this.data.next(item);
    this.dialogRef.close();
  }

}









