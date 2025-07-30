import { COMMA, ENTER } from '@angular/cdk/keycodes';
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { MatChipInputEvent } from '@angular/material/chips';
import { MetadataSpecProperty, MetadataSpecPropertyType } from 'src/app/api/models/token-spec-tab';
import { enumRegex } from '../metadata-specs-dialog.component';

@Component({
  selector: 'metadata-specs-dialog-field-type',
  templateUrl: './metadata-specs-dialog-field-type.component.html',
  styleUrls: ['./metadata-specs-dialog-field-type.component.css']
})
export class MetadataSpecsDialogFieldTypeComponent implements OnInit {
  @Input()
  field: MetadataSpecProperty;
  @Input()
  type: MetadataSpecPropertyType;
  @Input()
  index: number;
  @Output("openDefineObjectModal")
  openDefineObjectModal: EventEmitter<number> = new EventEmitter();
  @Output("onContentUpdate")
  onContentUpdate: EventEmitter<any> = new EventEmitter();
  @Output("onContentTypeUpdate")
  onContentTypeUpdate: EventEmitter<string> = new EventEmitter();
  @Output("onDefaultValueUpdate")
  onDefaultValueUpdate: EventEmitter<any> = new EventEmitter();
  @Output("onPlaceholderUpdate")
  onPlaceholderUpdate: EventEmitter<string> = new EventEmitter();

  readonly separatorKeysCodes = [ENTER, COMMA] as const;
  selectedArrayType = 'String';
  arrayTypes = ['String', 'Object', 'Number'];
  tagsArr = [];
  enumFields = [];
  enumError = false;
  enumValue = '';

  constructor() { }

  ngOnInit(): void {
    if (this.field.type === 'TAGS') {
      this.tagsArr = this.field.defaultValue ? this.field.defaultValue.split(',') : [];
    } else if (this.field.type === 'ARRAY') {
      // if (this.field.fieldContentType) {
      //   this.selectedArrayType = this.field.fieldContentType || 'String';
      // } else if (typeof this.field.content === 'object') {
      //   this.selectedArrayType = 'OBJECT';
      // } else if (typeof this.field.content === 'number') {
      //   this.selectedArrayType = 'NUMBER';
      // } else {
      //   this.selectedArrayType = 'STRING';
      // }
    }
  }

  isEnumValid() {
    return this.enumValue && enumRegex.test(this.enumValue);
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
    this.onPlaceholderUpdate.emit(value);
  }

  onDefaultValueChange(value: any) {
    this.onDefaultValueUpdate.emit(value);
  }
  onBooleanDefaultValueChange(value: any) {
    if (value) {
      this.onDefaultValueUpdate.emit('true');
    } else {
      this.onDefaultValueUpdate.emit('false');
    }
  }

  onNumberDefaultValueChange(value: number) {
    this.onDefaultValueUpdate.emit({
      index: this.index,
      otherProps: {
        defaultValue: +value,
      },
    });
  }

  onBooleanChange(state: boolean) {
    this.onDefaultValueUpdate.emit({
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
  parseDefaultBooleanValue(defaultValue: string): boolean {
    return defaultValue && defaultValue.toLowerCase() === 'true';
  }

  propertiesPrieview(properties: MetadataSpecProperty[]) {
    return (properties && properties.length > 0) ?
      properties.map(property => property.name) :
      ""
  }

  public readonly MetadataSpecPropertyType = MetadataSpecPropertyType;
}
