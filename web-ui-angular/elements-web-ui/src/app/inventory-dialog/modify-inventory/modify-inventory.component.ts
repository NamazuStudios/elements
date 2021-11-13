import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { InventoryService } from 'src/app/api/services/inventory.service';

@Component({
  selector: 'app-modify-inventory',
  templateUrl: './modify-inventory.component.html',
  styleUrls: ['./modify-inventory.component.css']
})
export class ModifyInventoryComponent implements OnInit {

  @Input()
  inventoryItems: [];

  @Output("refresh") 
  refresh: EventEmitter<any> = new EventEmitter();

  constructor(
    private inventoryService: InventoryService
  ) { }

  ngOnInit() {
  }

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
