import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Item } from 'src/app/api/models';
import { DistinctInventoryService } from 'src/app/api/services/distinct-inventory.service';
import { ItemSelectDialogComponent } from '../item-select-dialog/item-select-dialog.component';
import {ItemCategory} from "../../api/models/item";


@Component({
  selector: 'distinct-app-add-inventory',
  templateUrl: './distinct-add-inventory.component.html',
  styleUrls: ['./distinct-add-inventory.component.css']
})
export class DistinctAddInventoryComponent implements OnInit {

  @Input()
  item: DistinctInventoryItemCreateRequest;

  @Output("refresh")
  refresh: EventEmitter<any> = new EventEmitter();

  selectedItem: Item;

  constructor(
    private inventoryService: DistinctInventoryService,
    public dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit() {
    this.item = {
      itemId: null,
      userId: null,
      profileId: null,
      metadata: null
    }
  }

  createInventory(name: string){
    this.inventoryService.createInventoryItem({
      userId: this.item.userId,
      itemId: name
    }).subscribe(
      data => {
        this.refresh.emit();
        this.snackBar.open("Item Created", 'Dismiss', { duration: 3000, panelClass: ['green-snackbar'] });
      },
      err => {
        if(err.code === "DUPLICATE"){
          this.snackBar.open(`⚠ ERROR: Item already exists. Please select a unique Stack Priority.`, 'Dismiss', { duration: 3000 });
        } else {
          this.snackBar.open(`${err.code}`, 'Dismiss', { duration: 3000 });
        }
      }
    )
  }

  showFindItemDialog() {
    this.dialog.open(ItemSelectDialogComponent, {
      width: '500px',
      data: {
        category: ItemCategory.DISTINCT,
        next: result => {
          this.selectedItem = result;
        }
      }
    });
  }

}

interface DistinctInventoryItemCreateRequest {
  itemId: string,
  userId: string,
  profileId: string,
  metadata: any
}
