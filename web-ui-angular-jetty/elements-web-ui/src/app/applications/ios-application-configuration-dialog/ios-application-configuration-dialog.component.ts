import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar'
import {ProductBundle} from '../../api/models/product-bundle';
import {Form, FormBuilder, Validators} from '@angular/forms';
import {AlertService} from '../../alert.service';
import {ApplicationConfigurationTypes} from "../application-configuration-types";

@Component({
  selector: 'app-ios-application-configuration-dialog',
  templateUrl: './ios-application-configuration-dialog.component.html',
  styleUrls: ['./ios-application-configuration-dialog.component.css']
})
export class IosApplicationConfigurationDialogComponent implements OnInit {

  public productBundles: Array<ProductBundle>;

  appIdForm = this.formBuilder.group({
    id: [ this.data.applicationConfiguration.id ],
    name: [ this.data.applicationConfiguration.name, [Validators.required]],
    type: [ ApplicationConfigurationTypes.IOS_APP_STORE ],
    applicationId: [this.data.applicationConfiguration.applicationId, [Validators.required]],
    parent: [this.data.applicationConfiguration.parent]
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
