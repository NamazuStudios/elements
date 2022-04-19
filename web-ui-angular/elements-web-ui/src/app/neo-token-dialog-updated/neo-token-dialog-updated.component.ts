import { COMMA, ENTER } from '@angular/cdk/keycodes';
import { Component, OnInit } from '@angular/core';
import { MatChipInputEvent } from '@angular/material/chips';
import { MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-neo-token-dialog-updated',
  templateUrl: './neo-token-dialog-updated.component.html',
  styleUrls: ['./neo-token-dialog-updated.component.css']
})
export class NeoTokenDialogUpdatedComponent implements OnInit {

  readonly separatorKeysCodes = [ENTER, COMMA ] as const;
  tags = [];
  enums = [
    { key: 1, toolTip: 'AllowOnlyTrades1', label: 'AllowOnlyTrades1' },
    { key: 2, toolTip: 'AllowOnlyTrades2', label: 'AllowOnlyTrades2' },
    { key: 3, toolTip: 'AllowOnlyTrades3', label: 'AllowOnlyTrades3' },
  ];

  constructor(
    public dialogRef: MatDialogRef<NeoTokenDialogUpdatedComponent>,
  ) { }

  ngOnInit(): void {
  }

  add(event: MatChipInputEvent): void {
    const value = (event.value || '').trim();

    // Add our fruit
    if (value) {
      this.tags.push(value);
    }

    // Clear the input value
    event.chipInput!.clear();
  }

  remove(tag: string): void {
    const index = this.tags.indexOf(tag);

    if (index >= 0) {
      this.tags.splice(index, 1);
    }
  }

  close() {
    this.dialogRef.close();
  }
}
