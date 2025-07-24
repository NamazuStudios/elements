import { Component, Inject, OnInit } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';

@Component({
  selector: 'metadata-duplicate-dialog',
  templateUrl: './metadata-duplicate-dialog.component.html',
  styleUrls: ['./metadata-duplicate-dialog.component.css']
})
export class MetadataDuplicateDialogComponent implements OnInit {

  name = '';

  constructor(
    public dialogRef: MatDialogRef<MetadataDuplicateDialogComponent>,
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
