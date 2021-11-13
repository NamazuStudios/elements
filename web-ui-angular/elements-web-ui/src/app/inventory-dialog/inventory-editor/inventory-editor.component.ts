import {Component, Input, OnInit} from '@angular/core';
import { InventoryService } from 'src/app/api/services/inventory.service';

@Component({
  selector: 'app-inventory-editor',
  templateUrl: './inventory-editor.component.html',
  styleUrls: ['./inventory-editor.component.css']
})
export class InventoryEditorComponent implements OnInit {
  inventoryItems: [];

  @Input()
  userId: string;

  constructor(
    private inventoryService: InventoryService
    ) { }

  ngOnInit() {
    this.refresh();
  }

  refresh(){
    this.inventoryService.getInventoryAdvanced({userId: this.userId}).subscribe(
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
