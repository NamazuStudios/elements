import { COMMA, ENTER } from '@angular/cdk/keycodes';
import { Component, Input, OnInit } from '@angular/core';
import { MatChipInputEvent } from '@angular/material/chips';

import { TabType, TabTypes } from '../neo-smart-token-specs-dialog.component';

@Component({
  selector: 'app-neo-smart-token-specs-dialog-field-type',
  templateUrl: './neo-smart-token-specs-dialog-field-type.component.html',
  styleUrls: ['./neo-smart-token-specs-dialog-field-type.component.css']
})
export class NeoSmartTokenSpecsDialogFieldTypeComponent implements OnInit {

  @Input()
  type: TabTypes;

  selectedArrayType = 'String';
  arrayTypes = ['String', 'Object'];

  readonly separatorKeysCodes = [ENTER, COMMA] as const;
  tags = [];

  constructor() { }

  ngOnInit(): void { }

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

  selectArrayType(value: string) {
    this.selectedArrayType = value;
  }
}
