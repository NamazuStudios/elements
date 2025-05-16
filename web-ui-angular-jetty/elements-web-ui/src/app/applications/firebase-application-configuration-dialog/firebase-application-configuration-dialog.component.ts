import {Component, Inject, OnInit} from '@angular/core';
import {FormBuilder, Validators} from "@angular/forms";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {ApplicationConfigurationTypes} from "../application-configuration-types";

@Component({
  selector: 'app-firebase-application-configuration-dialog',
  templateUrl: './firebase-application-configuration-dialog.component.html',
  styleUrls: ['./firebase-application-configuration-dialog.component.css']
})
export class FirebaseApplicationConfigurationDialogComponent implements OnInit {

  configurationForm = this.formBuilder.group({
    id: [ this.data.applicationConfiguration.id ],
    name: [ this.data.applicationConfiguration.name, [Validators.required]],
    description: [this.data.applicationConfiguration.description, [Validators.required]],
    type: [ ApplicationConfigurationTypes.FIREBASE ],
    projectId: [ this.data.applicationConfiguration.projectId, Validators.required ],
    serviceAccountCredentials: [ this.data.applicationConfiguration.serviceAccountCredentials, Validators.required ],
    parent: this.formBuilder.group({
      id: [ this.data.applicationConfiguration.parent.id ]
    })
  });

  constructor(public dialogRef: MatDialogRef<FirebaseApplicationConfigurationDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any,
              private formBuilder: FormBuilder) { }

  ngOnInit() {}

}
