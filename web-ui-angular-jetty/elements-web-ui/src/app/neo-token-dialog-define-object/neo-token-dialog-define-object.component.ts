import { Component, Inject, OnInit } from '@angular/core';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MetadataSpecProperty, MetadataSpecPropertyType } from '../api/models/token-spec-tab';

@Component({
  selector: 'app-neo-token-dialog-define-object',
  templateUrl: './neo-token-dialog-define-object.component.html',
  styleUrls: ['./neo-token-dialog-define-object.component.css']
})
export class NeoTokenDialogDefineObjectComponent implements OnInit {

  fields = [];
  propertiesTypes = [];
  activeObjectIndex = null;
  expandedField = null;
  // Workaround for accordion animation on init
  disableAnimation = true;

  constructor(
    public dialogRef: MatDialogRef<NeoTokenDialogDefineObjectComponent>,
    public dialog: MatDialog,
    @Inject(MAT_DIALOG_DATA)
    public parentData: {
      updateProperties: Function,
      properties: MetadataSpecProperty[]
    },
  ) { }

  ngOnInit(): void {
    console.log('filling: ',this.parentData)
    if (this.parentData.properties) {
      console.log('filling: ',this.parentData.properties)
      this.fields = this.parentData.properties;
    } else {
      this.fields = [this.createField()];
    }
    this.propertiesTypes = Object.keys(MetadataSpecPropertyType).map(key => ({
      key,
      value: MetadataSpecPropertyType[key]
    }));
  }

  handleFieldPanelStateChange(index: number) {
    this.expandedField = index;
  }

  createField(): MetadataSpecProperty {
    return {
      name: '',
      displayName: '',
      required: false,
      type: MetadataSpecPropertyType.STRING,
      defaultValue: '',
      placeholder: '',
      properties: []
    };
  }

  removeField(fieldIndex: number): void {
    this.fields = this.fields.filter((_: MetadataSpecProperty, index: number): boolean => {
      return index !== fieldIndex;
    });
  }

  addNewField(): void {
    this.disableAnimation = true;
    this.fields = [...this.fields, this.createField()];
    setTimeout(() => this.disableAnimation = false, 10);
  }

  duplicateField(field: MetadataSpecProperty): void {
    if (field) {
      this.fields = [...this.fields, field];
    }
  }

  changeFieldName(target: EventTarget, fieldIndex: number) {
    this.fields = this.fields.map((field, index) => {
      if (fieldIndex === index && target) {
        return {
          ...field,
          name: (target as HTMLInputElement).value,
        }
      }
      return field;
    });
  }

  updateFieldType(type: MetadataSpecPropertyType, index: number): void {
    this.disableAnimation = true;
    this.fields = this.fields.map((field: MetadataSpecProperty, i: number): MetadataSpecProperty => {
      if (index === i) {
        return {
          ...field,
          type: MetadataSpecPropertyType[type],
        }
      }
      return field;
    });
    setTimeout(() => this.disableAnimation = false);
  }

  openDefineObjectModal(index: number): void {
    this.dialog.open(NeoTokenDialogDefineObjectComponent, {
      width: '800px',
      data: {
        updateProperties: this.updateProperties.bind(this),
      }
    });
    this.activeObjectIndex = index;
  }

  updateFieldContentByIndex(index: number, content: MetadataSpecProperty | string | null): MetadataSpecProperty[] {
    if (!content) return this.fields;
    return this.fields.map((field: MetadataSpecProperty, i: number) => {
      if (index === i) {
        return {
          ...field,
          content,
        }
      }
      return field;
    });
  }

  close(): void {
    this.dialogRef.close();
  }

  updateNonObjectField(data): void {
    this.fields = this.fields.map(
      (field: MetadataSpecProperty, index: number) => {
        if (index === data.index) {
          return {
            ...field,
            defaultValue: data?.otherProps?.defaultValue || field.defaultValue || '',
            placeHolder: data?.otherProps?.placeHolder || field.placeholder || '',
          }
        }
        return field;
      }
    );
  }

  updateProperties(fields: MetadataSpecProperty[]): void {
    this.fields = fields.map((field: MetadataSpecProperty, index: number): MetadataSpecProperty => {
      if (index === this.activeObjectIndex) {
        return {
          ...field,
        }
      }
      return field;
    });
    console.log('field ',this.fields);
  }

  submit(): void {
    this.parentData.updateProperties(this.fields);
    this.dialogRef.close();
  }
}
