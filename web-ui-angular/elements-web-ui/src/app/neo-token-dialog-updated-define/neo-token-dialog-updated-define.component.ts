import { Component, Inject, OnInit } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';

@Component({
  selector: 'app-neo-token-dialog-updated-define',
  templateUrl: './neo-token-dialog-updated-define.component.html',
  styleUrls: ['./neo-token-dialog-updated-define.component.css']
})
export class NeoTokenDialogUpdatedDefineComponent implements OnInit {

  constructor(
    public dialogRef: MatDialogRef<NeoTokenDialogUpdatedDefineComponent>,
    @Inject(MAT_DIALOG_DATA)
    public data: {
      content,
    },
  ) { }

  ngOnInit(): void {}

  close() {
    this.dialogRef.close();
  }
}
