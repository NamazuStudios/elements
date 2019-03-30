import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material';

@Component({
  selector: 'app-ios-application-configuration-dialog',
  templateUrl: './ios-application-configuration-dialog.component.html',
  styleUrls: ['./ios-application-configuration-dialog.component.css']
})
export class IosApplicationConfigurationDialogComponent implements OnInit {
  private productBundles: ProductBundle[];

  constructor(public dialogRef: MatDialogRef<IosApplicationConfigurationDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any) {
    console.log(data);
  }

  ngOnInit() {
  }

}
