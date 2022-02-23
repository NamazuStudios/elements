import {Component, Input, OnInit} from '@angular/core';
import {DistinctInventoryService} from "../../api/services/distinct-inventory.service";

@Component({
  selector: 'distinct-app-inventory-editor',
  templateUrl: './distinct-inventory-editor.component.html',
  styleUrls: ['./distinct-inventory-editor.component.css']
})
export class DistinctInventoryEditorComponent implements OnInit {
  inventoryItems: [];

  @Input()
  userId: string;

  constructor(
    private inventoryService: DistinctInventoryService
    ) { }

  ngOnInit() {
    this.refresh();
  }

  refresh(){
    this.inventoryService.getInventoryResponse({userId: this.userId}).subscribe(
      inventoryItems => {
        this.inventoryItems =
          (JSON.parse(JSON.stringify(inventoryItems)))
          .objects.map( inventoryItem => {
            return {
              inventoryItemId: inventoryItem.id,
              priority: inventoryItem.priority,
              quantity: inventoryItem.quantity,
              userId: inventoryItem.user.id,
              itemId: inventoryItem.item.id,
              name: inventoryItem.item.name,
            }
          });
      }
    )
  }
}
