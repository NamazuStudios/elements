import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef, MatSnackBar} from '@angular/material';
import {ProductBundle} from '../api/models/product-bundle';
import {Form, FormBuilder, Validators} from '@angular/forms';
import {AlertService} from '../alert.service';

@Component({
  selector: 'app-ios-application-configuration-dialog',
  templateUrl: './ios-application-configuration-dialog.component.html',
  styleUrls: ['./ios-application-configuration-dialog.component.css']
})
export class IosApplicationConfigurationDialogComponent implements OnInit {
  public productBundles: Array<ProductBundle>;

  appIdForm = this.formBuilder.group({
    applicationId: [this.data.applicationConfiguration.applicationId, [Validators.required]],
    category: ['IOS_APP_STORE'],
    parent: [this.data.applicationConfiguration.parent]
  });

  constructor(public dialogRef: MatDialogRef<IosApplicationConfigurationDialogComponent>, private formBuilder: FormBuilder,
              @Inject(MAT_DIALOG_DATA) public data: any, private alertService: AlertService, private snackBar: MatSnackBar) {
    this.data.applicationConfiguration.productBundles = this.data.applicationConfiguration.productBundles || [];
    this.productBundles = this.data.applicationConfiguration.productBundles;
  }

  close(saveChanges: boolean = false) {
    if (!saveChanges) {
      this.dialogRef.close();
      return;
    }

    const formData = this.appIdForm.value;
    formData.productBundles = this.productBundles;
    this.data.next(formData).subscribe(r => {
      this.dialogRef.close();
      this.data.refresher.refresh();
    }, err => {
      this.alertService.error(err);
    });
  }

  ngOnInit() {
  }

}
