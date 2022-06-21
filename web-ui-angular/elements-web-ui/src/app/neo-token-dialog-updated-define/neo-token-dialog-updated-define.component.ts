import { Component, Inject, OnInit } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';

@Component({
  selector: 'app-neo-token-dialog-updated-define',
  templateUrl: './neo-token-dialog-updated-define.component.html',
  styleUrls: ['./neo-token-dialog-updated-define.component.css']
})
export class NeoTokenDialogUpdatedDefineComponent implements OnInit {

  fields = [];

  constructor(
    public dialogRef: MatDialogRef<NeoTokenDialogUpdatedDefineComponent>,
    @Inject(MAT_DIALOG_DATA)
    public data: {
      content,
      isUpdate,
      index,
      onUpdate,
    },
  ) { }

  ngOnInit(): void {
    this.fields = this.data.content;
  }

  updateFieldValue(value: string, fieldIndex: number): void {
    this.fields = this.fields.map((field, index) => {
      if (fieldIndex === index) {
        return {
          ...field,
          value: value,
        }
      }
      return field;
    });
  }

  close() {
    this.dialogRef.close();
  }

  submit() {
    this.data.onUpdate(
      this.fields,
      {
        isUpdate: this.data.isUpdate,
        index: this.data.index,
      }
    );
    this.close();
  }
}
