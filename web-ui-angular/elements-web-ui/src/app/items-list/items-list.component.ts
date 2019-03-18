import {AfterViewInit, Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {Item} from '../api/models/item';
import {SelectionModel} from '@angular/cdk/collections';
import {ItemsDatasource} from '../items.datasource';
import {MatDialog, MatPaginator, MatTable} from '@angular/material';
import {ItemsService} from '../api/services/items.service';
import {AlertService} from '../alert.service';
import {ConfirmationDialogService} from '../confirmation-dialog/confirmation-dialog.service';
import {fromEvent} from 'rxjs';
import {debounceTime, distinctUntilChanged, filter, tap} from 'rxjs/operators';
import {ItemViewModel} from '../models/item-view-model';
import {ItemDialogComponent} from '../item-dialog/item-dialog.component';

@Component({
  selector: 'app-digital-goods-list',
  templateUrl: './items-list.component.html',
  styleUrls: ['./items-list.component.css']
})
export class ItemsListComponent implements OnInit, AfterViewInit {
  hasSelection = false;
  selection: SelectionModel<Item>;
  dataSource: ItemsDatasource;
  displayedColumns = ['select', 'id', 'name', 'actions'];
  currentItems: Item[];

  @ViewChild(MatPaginator) paginator: MatPaginator;
  @ViewChild('input') input: ElementRef;
  @ViewChild(MatTable) table: MatTable<Item>;

  constructor(private itemsService: ItemsService, private alertService: AlertService, private dialogService: ConfirmationDialogService, public dialog: MatDialog) { }

  ngOnInit() {
    this.selection = new SelectionModel<Item>(true, []);
    this.dataSource = new ItemsDatasource(this.itemsService);
    this.paginator.pageSize = 10;
    this.refresh(0);
  }

  ngAfterViewInit() {
    // server-side search
    fromEvent(this.input.nativeElement, 'keyup')
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
    this.dataSource.items$.subscribe(currentItems => this.currentItems = currentItems);
    this.dataSource.totalCount$.subscribe(totalCount => this.paginator.length = totalCount);
  }

  refresh(delay = 500) {
    setTimeout(() => {
      this.selection.clear();
      this.dataSource.loadItems(
        this.input.nativeElement.value,
        this.paginator.pageIndex * this.paginator.pageSize,
        this.paginator.pageSize);
    }, delay)
  }

  /** Whether the number of selected elements matches the total number of rows. */
  isAllSelected() {
    const numSelected = this.selection.selected.length;
    const numRows = this.currentItems.length;
    return numSelected == numRows;
  }

  /** Selects all rows if they are not all selected; otherwise clear selection. */
  masterToggle() {
    this.isAllSelected() ?
      this.selection.clear() :
      this.currentItems.forEach(row => this.selection.select(row));
  }

  deleteItem(item) {
    this.dialogService
      .confirm('Confirm Dialog', `Are you sure you want to delete the item '${item.name}'`)
      .pipe(filter(r => r))
      .subscribe(res => {
        this.doDeleteItem(item);
        this.refresh();
      });
  }

  doDeleteItem(item) {
    this.itemsService.deleteItem(item.id).subscribe(r => {},
      error => this.alertService.error(error));
  }

  deleteSelectedItems(){
    this.dialogService
      .confirm('Confirm Dialog', `Are you sure you want to delete the ${this.selection.selected.length} selected item${this.selection.selected.length == 1 ? '' : 's'}?`)
      .pipe(filter(r => r))
      .subscribe(res => {
        this.selection.selected.forEach(row => this.doDeleteItem(row));
        this.selection.clear();
        this.refresh();
      });
  }

  showDialog(isNew: boolean, item: Item, next) {
    const dialogRef = this.dialog.open(ItemDialogComponent, {
      width: '900px',
      data: { isNew: isNew, item: item }
    });

    dialogRef
      .afterClosed()
      .pipe(filter(r => r))
      .subscribe(next);
  }

  addItem() {
    this.showDialog(true, new ItemViewModel(), result => {
      this.itemsService.createItem(result).subscribe(r => {
          this.refresh();
        },
        error => this.alertService.error(error));
    });
  }

  editItem(item) {
    this.showDialog(false, item, res => {
      this.itemsService.updateItem({ identifier: item.id, body: res }).subscribe(r => {
          this.refresh();
        },
        error => this.alertService.error(error));
    });
  }
}
