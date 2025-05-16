import {Component, Inject, OnInit} from '@angular/core';
import {FormBuilder, Validators} from "@angular/forms";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {ApplicationConfigurationTypes} from "../application-configuration-types";

@Component({
  selector: 'app-matchmaking-application-configuration-dialog',
  templateUrl: './matchmaking-application-configuration-dialog.component.html',
  styleUrls: ['./matchmaking-application-configuration-dialog.component.css']
})
export class MatchmakingApplicationConfigurationDialogComponent implements OnInit {

  configurationForm = this.formBuilder.group({
    id: [ this.data.applicationConfiguration.id ],
    name: [ this.data.applicationConfiguration.name, [Validators.required]],
    type: [ ApplicationConfigurationTypes.MATCHMAKING ],
    description: [this.data.applicationConfiguration.description, [Validators.required]],
    useDefaultMatchmaker: [true],
    matchmaker: this.formBuilder.group({
      elementName: [ this.data.applicationConfiguration.matchmaker?.elementName || '', Validators.required ],
      serviceType: [ this.data.applicationConfiguration.matchmaker?.serviceType || '', Validators.required ],
      serviceName: [ this.data.applicationConfiguration.matchmaker?.serviceName || '', Validators.required ]
    }),
    defineSuccessCallback: [false],
    success: this.formBuilder.group({
      method: [ this.data.applicationConfiguration.success?.method, Validators.required ],
      service: this.formBuilder.group({
        elementName: [ this.data.applicationConfiguration.success?.service?.elementName, Validators.required ],
        serviceType: [ this.data.applicationConfiguration.success?.service?.serviceType, Validators.required ],
        serviceName: [ this.data.applicationConfiguration.success?.service?.serviceName, Validators.required ]
      })
    }),
    parent: this.formBuilder.group({
      id: [ this.data.applicationConfiguration.parent.id ]
    })
  });

  constructor(public dialogRef: MatDialogRef<MatchmakingApplicationConfigurationDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any,
              private formBuilder: FormBuilder) { }

  ngOnInit() {
    // Handle 'useDefaultMatchmaker' toggle
    this.configurationForm.get('useDefaultMatchmaker')?.valueChanges.subscribe((useDefault) => {
      const matchmakerGroup = this.configurationForm.get('matchmaker');
      if (useDefault) {
        matchmakerGroup?.disable(); // Disable matchmaker group if default is used
      } else {
        matchmakerGroup?.enable(); // Enable matchmaker group if not using default
      }
    });

    // Handle 'defineSuccessCallback' toggle
    this.configurationForm.get('defineSuccessCallback')?.valueChanges.subscribe((defineCallback) => {
      const successGroup = this.configurationForm.get('success');
      if (defineCallback) {
        successGroup?.enable(); // Enable success group if callback is defined
      } else {
        successGroup?.disable(); // Disable success group if callback is not defined
      }
    });

    // Initialize the state of the toggles
    if (this.configurationForm.get('useDefaultMatchmaker')?.value) {
      this.configurationForm.get('matchmaker')?.disable();
    }
    if (!this.configurationForm.get('defineSuccessCallback')?.value) {
      this.configurationForm.get('success')?.disable();
    }
  }

  save() {

    const data = this.configurationForm.value;

    if (data.useDefaultMatchmaker) {
      data.matchmaker = null;
    }

    if (!data.defineSuccessCallback) {
      data.success = null;
    }

    delete data.useDefaultMatchmaker;
    delete data.defineSuccessCallback;

    this.dialogRef.close(data)

  }

  cancel() {
    this.dialogRef.close()
  }

}
