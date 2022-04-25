import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';

import { FieldTypes, TabField } from '../neo-smart-token-specs-dialog.component';

@Component({
  selector: 'app-neo-smart-token-specs-dialog-field-type',
  templateUrl: './neo-smart-token-specs-dialog-field-type.component.html',
  styleUrls: ['./neo-smart-token-specs-dialog-field-type.component.css']
})
export class NeoSmartTokenSpecsDialogFieldTypeComponent implements OnInit {
  @Input()
  field: TabField;
  @Input()
  type: FieldTypes;
  @Input()
  index: number;
  @Output("openDefineObjectModal")
  openDefineObjectModal: EventEmitter<number> = new EventEmitter();
  @Output("onContentUpdate")
  onContentUpdate: EventEmitter<any> = new EventEmitter();

  selectedArrayType = 'String';
  arrayTypes = ['String', 'Object'];


  constructor() { }

  ngOnInit(): void {}

  selectArrayType(value: string) {
    this.selectedArrayType = value;
  }

  openDefineModal() {
    this.openDefineObjectModal.emit(this.index);
  }

  onBooleanChange(state: boolean) {
    this.onContentUpdate.emit({
      index: this.index,
      content: state,
    });
  }

  onEnumChange(value: string) {
    this.onContentUpdate.emit({
      index: this.index,
      content: value,
    });
  }
}
