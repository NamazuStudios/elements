import { COMMA, ENTER } from '@angular/cdk/keycodes';
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { MatChipInputEvent } from '@angular/material/chips';
import { TokenSpecTabField, TokenSpecTabFieldTypes } from 'src/app/api/models/token-spec-tab';

@Component({
  selector: 'app-neo-smart-token-specs-dialog-field-type',
  templateUrl: './neo-smart-token-specs-dialog-field-type.component.html',
  styleUrls: ['./neo-smart-token-specs-dialog-field-type.component.css']
})
export class NeoSmartTokenSpecsDialogFieldTypeComponent implements OnInit {
  @Input()
  field: TokenSpecTabField;
  @Input()
  type: TokenSpecTabFieldTypes;
  @Input()
  index: number;
  @Output("openDefineObjectModal")
  openDefineObjectModal: EventEmitter<number> = new EventEmitter();
  @Output("onContentUpdate")
  onContentUpdate: EventEmitter<any> = new EventEmitter();
  @Output("onContentTypeUpdate")
  onContentTypeUpdate: EventEmitter<string> = new EventEmitter();

  readonly separatorKeysCodes = [ENTER, COMMA] as const;
  selectedArrayType = 'String';
  arrayTypes = ['String', 'Object'];
  tagsArr = [];
  enumFields = [];
  enumError = false;
  enumValue = '';

  constructor() { }

  ngOnInit(): void {
    if (this.field.fieldType === 'Enum') {
      this.enumFields = this.field?.content ? this.field?.content.split(',') : [];
    } else if (this.field.fieldType === 'Tags') {
      this.tagsArr = this.field.defaultValue ? this.field.defaultValue.split(',') : [];
    } else if (this.field.fieldType === 'Array') {
      if (this.field.fieldContentType) {
        this.selectedArrayType = this.field.fieldContentType || 'String';
      } else if (typeof this.field.content === 'object') {
        this.selectedArrayType = 'Object';
      } else {
        this.selectedArrayType = 'String';
      }
    }
  }

  add(event: MatChipInputEvent): void {
    const value = (event.value || '').trim();

    if (value) {
      const tags = this.field.defaultValue ? [...this.field.defaultValue.split(','), value] : [value];
      this.tagsArr = tags;
      this.onDefaultValueChange(tags.join(','));
    }

    // Clear the input value
    event.chipInput!.clear();
  }

  remove(tag: string): void {
    const tags = this.field.defaultValue.split(',');
    const index = tags.indexOf(tag);

    if (index >= 0) {
      tags.splice(index, 1);
      this.tagsArr = tags;
      this.onDefaultValueChange(tags.join(','));
    }
  }

  selectArrayType(value: string) {
    this.onContentTypeUpdate.emit(value);
  }

  openDefineModal() {
    this.openDefineObjectModal.emit(this.index);
  }

  changeEnumValue(value: string) {
    this.enumValue = value;
  }

  onPlaceholderChange(value: string) {
    this.onContentUpdate.emit({
      index: this.index,
      otherProps: {
        placeHolder: value,
      }
    });
  }

  onDefaultValueChange(value: string) {
    this.onContentUpdate.emit({
      index: this.index,
      otherProps: {
        defaultValue: value,
      },
    });
  }

  onBooleanChange(state: boolean) {
    this.onContentUpdate.emit({
      index: this.index,
      content: state,
    });
  }

  onEnumChange() {
    this.onContentUpdate.emit({
      index: this.index,
      content: [...this.enumFields, this.enumValue].join(','),
    });
    this.enumValue = '';
  }

  removeEnumField(index: number) {
    this.onContentUpdate.emit({
      index: this.index,
      content: this.enumFields.filter((_, fieldIndex: number) => index !== fieldIndex).join(','),
    });
  }
}
