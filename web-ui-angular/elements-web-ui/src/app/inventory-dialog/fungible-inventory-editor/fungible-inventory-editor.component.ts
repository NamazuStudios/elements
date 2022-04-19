import {Component, Input, OnInit} from '@angular/core';
import { FungibleInventoryService } from 'src/app/api/services/fungible-inventory.service';

@Component({
  selector: 'fungible-app-inventory-editor',
  templateUrl: './fungible-inventory-editor.component.html',
  styleUrls: ['./fungible-inventory-editor.component.css']
})
export class FungibleInventoryEditorComponent implements OnInit {
  inventoryItems: [];

  @Input()
  userId: string;

  constructor(
    private inventoryService: FungibleInventoryService
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
