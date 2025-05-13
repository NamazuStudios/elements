import {Component, Inject, OnInit} from '@angular/core';
import {ProductBundle} from '../../api/models/product-bundle';
import {FormBuilder, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {AlertService} from '../../alert.service';
import {ApplicationConfigurationTypes} from "../application-configuration-types";

@Component({
  selector: 'app-android-google-play-configuration-dialog',
  templateUrl: './android-google-play-configuration-dialog.component.html',
  styleUrls: ['./android-google-play-configuration-dialog.component.css']
})
export class AndroidGooglePlayConfigurationDialogComponent implements OnInit {

  public productBundles: Array<ProductBundle>;

  appInfoForm = this.formBuilder.group({
    id: [ this.data.applicationConfiguration.id ],
    name: [ this.data.applicationConfiguration.name, [Validators.required]],
    type: [ ApplicationConfigurationTypes.ANDROID_GOOGLE_PLAY ],
    description: [this.data.applicationConfiguration.description, [Validators.required]],
    parent: [this.data.applicationConfiguration.parent],
    applicationId: [this.data.applicationConfiguration.applicationId, [Validators.required]],
    jsonKey: [JSON.stringify(this.data.applicationConfiguration.jsonKey), [Validators.required]]
  });

  constructor(public dialogRef: MatDialogRef<AndroidGooglePlayConfigurationDialogComponent>,
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

    const formData = this.appInfoForm.value;
    formData.productBundles = this.productBundles;
    try {
      formData.jsonKey = JSON.parse(formData.jsonKey);
    } catch (e) {
      this.alertService.error(e);
      return;
    }

    this.dialogRef.close(formData);
  }

  ngOnInit() {}

}
