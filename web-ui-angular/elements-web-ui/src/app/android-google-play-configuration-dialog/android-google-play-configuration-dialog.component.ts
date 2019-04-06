import {Component, Inject, OnInit} from '@angular/core';
import {ProductBundle} from '../api/models/product-bundle';
import {FormBuilder, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogRef, MatSnackBar} from '@angular/material';
import {AlertService} from '../alert.service';

@Component({
  selector: 'app-android-google-play-configuration-dialog',
  templateUrl: './android-google-play-configuration-dialog.component.html',
  styleUrls: ['./android-google-play-configuration-dialog.component.css']
})
export class AndroidGooglePlayConfigurationDialogComponent implements OnInit {
  public productBundles: Array<ProductBundle>;

  appInfoForm = this.formBuilder.group({
    applicationId: [this.data.applicationConfiguration.applicationId, [Validators.required]],
    jsonKey: [this.data.applicationConfiguration.jsonKey, [Validators.required]],
    category: ['IOS_APP_STORE'],
    parent: [this.data.applicationConfiguration.parent]
  });

  constructor(public dialogRef: MatDialogRef<AndroidGooglePlayConfigurationDialogComponent>, private formBuilder: FormBuilder,
              @Inject(MAT_DIALOG_DATA) public data: any, private alertService: AlertService, private snackBar: MatSnackBar) {
    this.data.applicationConfiguration.productBundles = this.data.applicationConfiguration.productBundles || [];
    this.productBundles = this.data.applicationConfiguration.productBundles;
  }

  close(saveChanges: boolean = false) {
    if (!saveChanges) {
      this.dialogRef.close();
      return;
    }

    const formData = this.appInfoForm.value;
    formData.productBundles = this.productBundles;
    this.dialogRef.close(formData);
  }

  ngOnInit() {
  }

}
