import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FungibleInventoryService } from 'src/app/api/services/fungible-inventory.service';

@Component({
  selector: 'fungible-app-modify-inventory',
  templateUrl: './fungible-modify-inventory.component.html',
  styleUrls: ['./fungible-modify-inventory.component.css']
})
export class FungibleModifyInventoryComponent implements OnInit {

  @Input()
  inventoryItems: [];

  @Output("refresh")
  refresh: EventEmitter<any> = new EventEmitter();

  constructor(
    private inventoryService: FungibleInventoryService
  ) { }

  ngOnInit() {}

  editInventoryQuantity(inventoryItemId, quantity){
    this.inventoryService.updateInventoryItemAdvanced({identifier: inventoryItemId, body: {quantity}}).subscribe(
      data => this.refresh.emit()
    );
  }

  deleteInventory(inventoryItemId: string) {
    this.inventoryService.deleteInventoryItemAdvanced(inventoryItemId).subscribe(
      data => this.refresh.emit()
    );
  }

  compare = (a, b) => a.name.localeCompare(b.name) || a.priority - b.priority;

}
