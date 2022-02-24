import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import {DistinctInventoryService} from "../../api/services/distinct-inventory.service";

@Component({
  selector: 'distinct-app-modify-inventory',
  templateUrl: './distinct-modify-inventory.component.html',
  styleUrls: ['./distinct-modify-inventory.component.css']
})
export class DistinctModifyInventoryComponent implements OnInit {

  @Input()
  inventoryItems: [];

  @Output("refresh")
  refresh: EventEmitter<any> = new EventEmitter();

  constructor(
    private inventoryService: DistinctInventoryService
  ) { }

  ngOnInit() {}

  saveInventory(item){
    this.inventoryService.updateInventoryItem(item.id, {
      userId: item.userId,
      profileId: item.profileId,
      metadata: item.metadata,
    }).subscribe(
      data => this.refresh.emit()
    );
  }

  deleteInventory(item) {
    this.inventoryService.deleteInventoryItem(item.id).subscribe(
      data => this.refresh.emit()
    );
  }

  compare = (a, b) => a.name.localeCompare(b.name) || a.priority - b.priority;

  getProfileLabel(item): string {
    return item.profile?.displayName ?? "(No Profile)";
  }

}
