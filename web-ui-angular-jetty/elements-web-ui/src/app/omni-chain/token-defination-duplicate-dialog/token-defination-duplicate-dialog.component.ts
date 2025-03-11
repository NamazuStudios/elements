import { Component, Inject, OnInit } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';

@Component({
  selector: 'app-token-defination-duplicate-dialog',
  templateUrl: './token-defination-duplicate-dialog.component.html',
  styleUrls: ['./token-defination-duplicate-dialog.component.css']
})
export class TokenDefinationDuplicateDialogComponent implements OnInit {

  name = '';

  constructor(
    public dialogRef: MatDialogRef<TokenDefinationDuplicateDialogComponent>,
    @Inject(MAT_DIALOG_DATA)
    public data: {
      submit: Function,
    },
  ) { }

  ngOnInit(): void {
  }

  changeTokenName(value: string): void {
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
