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

  @Input()
  profileId: string;

  constructor(
    private inventoryService: DistinctInventoryService
    ) { }

  ngOnInit() {
    this.refresh();
  }

  refresh() {
    this.inventoryService.getInventory({userId: this.userId, profileId: this.profileId }).subscribe(
      inventoryItems => {
        this.inventoryItems =
          (JSON.parse(JSON.stringify(inventoryItems)))
          .objects.map( inventoryItem => {
            return {
              id: inventoryItem.id,
              userId: inventoryItem.user.id,
              itemId: inventoryItem.item.id,
              profile: inventoryItem.profile,
              name: inventoryItem.item.name,
              metadata: inventoryItem.metadata
            }
          });
      }
    )
  }
}
