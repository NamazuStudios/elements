import {Component, Inject, OnInit} from '@angular/core';
import {FormBuilder, Validators} from "@angular/forms";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material";

@Component({
  selector: 'app-matchmaking-application-configuration-dialog',
  templateUrl: './matchmaking-application-configuration-dialog.component.html',
  styleUrls: ['./matchmaking-application-configuration-dialog.component.css']
})
export class MatchmakingApplicationConfigurationDialogComponent implements OnInit {

  configurationForm = this.formBuilder.group({
    category: [ 'MATCHMAKING' ],
    algorithm: 'FIFO',
    scheme: [ this.data.applicationConfiguration.scheme, Validators.required ],
    success: this.formBuilder.group({
      module: [ this.data.applicationConfiguration.success.module, Validators.required ],
      method: [ this.data.applicationConfiguration.success.method, Validators.required ]
    }),
    parent: this.formBuilder.group({
      id: [ this.data.applicationConfiguration.parent.id ]
    })
  });

  constructor(public dialogRef: MatDialogRef<MatchmakingApplicationConfigurationDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any,
              private formBuilder: FormBuilder) { }

  ngOnInit() {
  }
}
