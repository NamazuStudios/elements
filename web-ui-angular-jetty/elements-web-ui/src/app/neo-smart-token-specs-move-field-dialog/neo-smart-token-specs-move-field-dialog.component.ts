import { Component, Inject, OnInit } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';

@Component({
  selector: 'app-neo-smart-token-specs-move-field-dialog',
  templateUrl: './neo-smart-token-specs-move-field-dialog.component.html',
  styleUrls: ['./neo-smart-token-specs-move-field-dialog.component.css']
})
export class NeoSmartTokenSpecsMoveFieldDialogComponent implements OnInit {

  selectedTabIndex: number;
  error: boolean;

  constructor(
    public dialogRef: MatDialogRef<NeoSmartTokenSpecsMoveFieldDialogComponent>,
    @Inject(MAT_DIALOG_DATA)
    public data: {
      fieldIndex: number,
      activeTabIndex: number,
      max: number,
      changeFieldTab: Function,
    },
  ) { }

  ngOnInit(): void {
    this.selectedTabIndex = this.data.activeTabIndex;
  }

  close() {
    this.dialogRef.close();
  }

  changeSelectedTabIndex(value: string) {
    this.selectedTabIndex = parseInt(value);
    if (this.selectedTabIndex > this.data.max) {
      this.error = true;
    }
    else {
      this.error = false;
    }
  }

  submit() {
    this.data.changeFieldTab(
      this.data.fieldIndex,
      this.selectedTabIndex
    );
    this.close();
  }
}
