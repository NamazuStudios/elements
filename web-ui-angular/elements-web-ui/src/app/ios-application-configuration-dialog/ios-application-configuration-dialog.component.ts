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
    parent: [this.data.applicationConfiguration.parent],
    appleSignInConfiguration: this.formBuilder.group({
      teamId: [
        this.data.applicationConfiguration.appleSignInConfiguration ?
        this.data.applicationConfiguration.appleSignInConfiguration.teamId : ""
      ],
      clientId: [
        this.data.applicationConfiguration.appleSignInConfiguration ?
        this.data.applicationConfiguration.appleSignInConfiguration.clientId : ""
      ],
      keyId: [
        this.data.applicationConfiguration.appleSignInConfiguration ?
        this.data.applicationConfiguration.appleSignInConfiguration.keyId : ""
      ],
      appleSignInPrivateKey: [
        this.data.applicationConfiguration.appleSignInConfiguration ?
        this.data.applicationConfiguration.appleSignInConfiguration.appleSignInPrivateKey : ""
      ]
    })
  });

  constructor(public dialogRef: MatDialogRef<IosApplicationConfigurationDialogComponent>,
              private formBuilder: FormBuilder,
              @Inject(MAT_DIALOG_DATA) public data: any,
              private alertService: AlertService,
              private snackBar: MatSnackBar) {
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
    this.dialogRef.close(formData);
  }

  ngOnInit() {}

}
