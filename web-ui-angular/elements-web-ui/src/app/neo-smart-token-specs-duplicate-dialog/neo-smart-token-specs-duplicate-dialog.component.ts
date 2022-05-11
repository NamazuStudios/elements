import { Component, Inject, OnInit } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';

@Component({
  selector: 'app-neo-smart-token-specs-duplicate-dialog',
  templateUrl: './neo-smart-token-specs-duplicate-dialog.component.html',
  styleUrls: ['./neo-smart-token-specs-duplicate-dialog.component.css']
})
export class NeoSmartTokenSpecsDuplicateDialogComponent implements OnInit {

  name = '';

  constructor(
    public dialogRef: MatDialogRef<NeoSmartTokenSpecsDuplicateDialogComponent>,
    @Inject(MAT_DIALOG_DATA)
    public data: {
      submit: Function,
    },
  ) { }

  ngOnInit(): void {
  }

  changeTemplateName(value: string): void {
    this.name = value;
  }

  close(): void {
    this.dialogRef.close();
  }

  submit(): void {
    this.data.submit(this.name);
    this.close();
  }
}
