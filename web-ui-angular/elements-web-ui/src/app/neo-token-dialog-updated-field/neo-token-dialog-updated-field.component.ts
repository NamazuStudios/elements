import { COMMA, ENTER } from '@angular/cdk/keycodes';
import { Component, Input, OnInit } from '@angular/core';
import { MatChipInputEvent } from '@angular/material/chips';
import { MatDialog } from '@angular/material/dialog';
import { TokenSpecTabField } from '../api/models/token-spec-tab';
import { NeoTokenDialogUpdatedDefineComponent } from '../neo-token-dialog-updated-define/neo-token-dialog-updated-define.component';

@Component({
  selector: 'app-neo-token-dialog-updated-field',
  templateUrl: './neo-token-dialog-updated-field.component.html',
  styleUrls: ['./neo-token-dialog-updated-field.component.css']
})
export class NeoTokenDialogUpdatedFieldComponent implements OnInit {

  @Input()
  field: TokenSpecTabField;

  tags = [];
  enumValues = [];
  arr = [];
  arrValue = '';
  readonly separatorKeysCodes = [ENTER, COMMA ] as const;

  constructor(
    public dialog: MatDialog,
  ) { }

  ngOnInit(): void {
    if (this.field.fieldType === 'Enum') {
      this.enumValues = this.field.content.replaceAll(' ', '').split(',') || [];
    }
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

  changeArrValue(value: string): void {
    this.arrValue = value;
  }

  addArrItem(): void {
    this.arr = [...this.arr, this.arrValue];
    this.arrValue = '';
  }

  removeArrItem(index: number): void {
    this.arr = this.arr.filter((_, i) => index !== i);
  }

  changeArrItem(value: string, index: number): void {
    this.arr = this.arr.map((item: string, i: number): string => {
      if (index === i) return value;
      return item;
    })
  }

  openDefineModal() {
    this.dialog.open(NeoTokenDialogUpdatedDefineComponent, {
      width: "800px",
      maxHeight: "90vh",
      data: {
        content: this.field.content,
      },
    });
  }
}
