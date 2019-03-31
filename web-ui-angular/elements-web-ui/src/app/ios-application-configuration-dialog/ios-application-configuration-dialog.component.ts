import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef, MatSnackBar} from '@angular/material';
import {ProductBundle} from '../api/models/product-bundle';
import {FormBuilder} from '@angular/forms';
import {AlertService} from '../alert.service';

@Component({
  selector: 'app-ios-application-configuration-dialog',
  templateUrl: './ios-application-configuration-dialog.component.html',
  styleUrls: ['./ios-application-configuration-dialog.component.css']
})
export class IosApplicationConfigurationDialogComponent implements OnInit {
  private productBundles: Array<ProductBundle>;

  constructor(public dialogRef: MatDialogRef<IosApplicationConfigurationDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any, private alertService: AlertService, private snackBar: MatSnackBar) {
    this.productBundles = this.data.applicationConfiguration.productBundles;
  }

  close(saveChanges: boolean) {
    if (!saveChanges) {
      this.dialogRef.close();
      return;
    }

    this.data.next(this.data.applicationConfiguration).subscribe(r => {
      this.dialogRef.close();
      this.data.refresher.refresh();
    }, err => {
      this.alertService.error(err);
    });
  }

  ngOnInit() {
  }

}
