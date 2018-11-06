import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material";
import {FormBuilder, FormControl, Validators} from "@angular/forms";

@Component({
  selector: 'app-application-dialog',
  templateUrl: './application-dialog.component.html',
  styleUrls: ['./application-dialog.component.css']
})
export class ApplicationDialogComponent implements OnInit {

  applicationForm = this.formBuilder.group({
    name: [ this.data.application.name, [Validators.required, Validators.pattern('^[a-zA-Z0-9]+$') ]],
    description: [ this.data.application.description ],
    scriptRepoUrl: [ { value: this.data.application.scriptRepoUrl, disabled: true } ]
  });

  constructor(public dialogRef: MatDialogRef<ApplicationDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any,
              private formBuilder: FormBuilder) { }

  ngOnInit() {
  }
}
