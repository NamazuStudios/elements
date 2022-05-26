import { COMMA, ENTER } from '@angular/cdk/keycodes';
import { Component, Input, OnInit, Output, EventEmitter } from '@angular/core';
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
  arrValue = '';
  enumValues = [];
  readonly separatorKeysCodes = [ENTER, COMMA ] as const;
  @Output("updateFieldValue")
  updateFieldValue: EventEmitter<any> = new EventEmitter();

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

    if (value) {
      const tags = this.field.value ? [...this.field.value, value] : [value];
      this.changeValue(tags);
    }

    // Clear the input value
    event.chipInput!.clear();
  }

  remove(tag: string): void {
    const index = this.field.value.indexOf(tag);

    if (index >= 0) {
      const tags = [...this.field.value];
      tags.splice(index, 1);
      this.changeValue(tags);
    }
  }

  changeArrValue(value: string): void {
    this.arrValue = value;
  }

  addArrItem(): void {
    const newArr = this.field.value ? [...this.field.value, this.arrValue] : [this.arrValue];
    this.arrValue = '';
    this.changeValue(newArr);
  }

  removeArrItem(index: number): void {
    const newArr = this.field.value.filter((_, i) => index !== i);
    this.changeValue(newArr);
  }

  changeArrItem(value: string, index: number): void {
    const newArr = (this.field.value || []).map((item: string, i: number): string => {
      if (index === i) return value;
      return item;
    });
    this.changeValue(newArr);
  }

  changeValue(value: string | string[]): void {
    this.updateFieldValue.emit(value);
  }

  onUpdate(data, params) {
    if (this.field.fieldType === 'Array') {
      if (!params?.isUpdate) {
        this.arrValue = data;
        this.addArrItem();
      } else {
        this.changeArrItem(data, params.index);
      }
    } else if (this.field.fieldType === 'Object') {
      this.changeValue(data);
    }
  }

  openDefineModal(item, index) {
    this.dialog.open(NeoTokenDialogUpdatedDefineComponent, {
      width: "800px",
      maxHeight: "90vh",
      data: {
        isUpdate: !!item,
        index,
        content: item || this.field.content,
        onUpdate: this.onUpdate.bind(this),
      },
    });
  }
}
