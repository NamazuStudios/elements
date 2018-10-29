import {Component, Inject, OnInit} from '@angular/core';
import {FormBuilder, Validators} from "@angular/forms";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material";

@Component({
  selector: 'app-facebook-application-configuration-dialog',
  templateUrl: './facebook-application-configuration-dialog.component.html',
  styleUrls: ['./facebook-application-configuration-dialog.component.css']
})
export class FacebookApplicationConfigurationDialogComponent implements OnInit {

  configurationForm = this.formBuilder.group({
    applicationId: [ this.data.applicationConfiguration.applicationId, Validators.required ],
    applicationSecret: [ this.data.applicationConfiguration.applicationSecret, Validators.required ]
  });

  constructor(public dialogRef: MatDialogRef<FacebookApplicationConfigurationDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any,
              private formBuilder: FormBuilder) { }

  ngOnInit() {
  }

}
