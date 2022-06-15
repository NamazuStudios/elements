import { Component, Inject, OnInit } from '@angular/core';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { TokenSpecTabField, TokenSpecTabFieldTypes } from '../api/models/token-spec-tab';

@Component({
  selector: 'app-neo-token-dialog-define-object',
  templateUrl: './neo-token-dialog-define-object.component.html',
  styleUrls: ['./neo-token-dialog-define-object.component.css']
})
export class NeoTokenDialogDefineObjectComponent implements OnInit {

  fields = [];
  fieldTypes = [];
  activeObjectIndex = null;
  expandedField = null;
  // Workaround for accordion animation on init
  disableAnimation = true;

  constructor(
    public dialogRef: MatDialogRef<NeoTokenDialogDefineObjectComponent>,
    public dialog: MatDialog,
    @Inject(MAT_DIALOG_DATA)
    public data: {
      updateFieldsWithContent: Function,
      content: TokenSpecTabField[],
    },
  ) { }

  ngOnInit(): void {
    if (this.data.content) {
      this.fields = this.data.content;
    } else {
      this.fields = [this.createField()];
    }
    this.fieldTypes = Object.keys(TokenSpecTabFieldTypes).map(key => ({
      key,
      value: TokenSpecTabFieldTypes[key]
    }));
  }

  ngAfterViewInit(): void {
    // timeout required to avoid the dreaded 'ExpressionChangedAfterItHasBeenCheckedError'
    setTimeout(() => this.disableAnimation = false);
  }

  handleFieldPanelStateChange(index: number) {
    this.expandedField = index;
    console.log(this.expandedField);
  }

  createField(): TokenSpecTabField {
    return {
      name: "",
      fieldType: TokenSpecTabFieldTypes.STRING,
      content: "",
    };
  }

  removeField(fieldIndex: number): void {
    this.fields = this.fields.filter((_: TokenSpecTabField, index: number): boolean => {
      return index !== fieldIndex;
    });
  }

  addNewField(): void {
    this.disableAnimation = true;
    this.fields = [...this.fields, this.createField()];
    setTimeout(() => this.disableAnimation = false, 10);
  }

  duplicateField(field: TokenSpecTabField): void {
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

  updateFieldType(type: TokenSpecTabFieldTypes, index: number): void {
    this.disableAnimation = true;
    this.fields = this.fields.map((field: TokenSpecTabField, i: number): TokenSpecTabField => {
      if (index === i) {
        return {
          ...field,
          fieldType: TokenSpecTabFieldTypes[type],
        }
      }
      return field;
    });
    setTimeout(() => this.disableAnimation = false);
  }

  openDefineObjectModal(index: number): void {
    this.dialog.open(NeoTokenDialogDefineObjectComponent, {
      width: "800px",
      data: {
        updateFieldsWithContent: this.updateFieldsWithContent.bind(this),
      }
    });
    this.activeObjectIndex = index;
  }

  updateFieldContentByIndex(index: number, content: TokenSpecTabField | string | null): TokenSpecTabField[] {
    if (!content) return this.fields;
    return this.fields.map((field: TokenSpecTabField, i: number) => {
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
      (field: TokenSpecTabField, index: number) => {
        if (index === data.index) {
          return {
            ...field,
            content: data.content || field.content,
            defaultValue: data?.otherProps?.defaultValue || field.defaultValue || '',
            placeHolder: data?.otherProps?.placeHolder || field.placeHolder || '',
          }
        }
        return field;
      }
    );
  }

  updateFieldsWithContent(fields: TokenSpecTabField[]): void {
    this.fields = this.fields.map((field: TokenSpecTabField, index: number): TokenSpecTabField => {
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
