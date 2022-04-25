import { Component, Inject, OnInit } from '@angular/core';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { FieldTypes } from '../neo-smart-token-specs-dialog/neo-smart-token-specs-dialog.component';

type Content = any;

interface Field {
  name: string;
  type: FieldTypes;
  content: Content;
}

@Component({
  selector: 'app-neo-token-dialog-define-object',
  templateUrl: './neo-token-dialog-define-object.component.html',
  styleUrls: ['./neo-token-dialog-define-object.component.css']
})
export class NeoTokenDialogDefineObjectComponent implements OnInit {

  fields = [];
  fieldTypes = [];
  activeObjectIndex = null;

  constructor(
    public dialogRef: MatDialogRef<NeoTokenDialogDefineObjectComponent>,
    public dialog: MatDialog,
    @Inject(MAT_DIALOG_DATA)
    public data: {
      updateFieldsWithContent: Function,
    },
  ) { }

  ngOnInit(): void {
    this.fields = [this.createField()];
    this.fieldTypes = Object.keys(FieldTypes).map(key => ({
      key,
      value: FieldTypes[key]
    }));
  }

  createField(): Field {
    return {
      name: "",
      type: FieldTypes.STRING,
      content: "",
    };
  }

  removeField(fieldIndex: number): void {
    this.fields = this.fields.filter((_: Field, index: number): boolean => {
      return index !== fieldIndex;
    });
  }

  addNewField(): void {
    this.fields = [...this.fields, this.createField()];
  }

  duplicateField(field: Field): void {
    if (field) {
      this.fields = [...this.fields, field];
    }
  }

  changeFieldName(value: string, fieldIndex: number) {
    this.fields = this.fields.map((field, index) => {
      if (fieldIndex === index) {
        return {
          ...field,
          name: value,
        }
      }
      return field;
    });
  }

  updateFieldType(type: FieldTypes, index: number): void {
    this.fields = this.fields.map((field: Field, i: number): Field => {
      if (index === i) {
        return {
          ...field,
          type: FieldTypes[type],
        }
      }
      return field;
    });
  }

  openDefineObjectModal(index: number): void {
    console.log('INDEX', index);
    this.dialog.open(NeoTokenDialogDefineObjectComponent, {
      width: "800px",
      data: {
        updateFieldsWithContent: this.updateFieldsWithContent.bind(this),
      }
    });
    this.activeObjectIndex = index;
  }

  updateFieldContentByIndex(index: number, content: Field | string | null): Field[] {
    if (!content) return this.fields;
    return this.fields.map((field: Field, i: number) => {
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
      (field: Field, index: number) => {
        if (index === data.index) {
          return {
            ...field,
            content: data.content,
          }
        }
        return field;
      }
    );
  }

  updateFieldsWithContent(fields: Field[]): void {
    this.fields = this.fields.map((field: Field, index: number): Field => {
      if (index === this.activeObjectIndex) {
        return {
          ...field,
          content: fields,
        }
      }
      return field;
    });
  }

  submit(): void {
    console.log(this.fields);
    this.data.updateFieldsWithContent(this.fields);
    this.dialogRef.close();
  }
}
