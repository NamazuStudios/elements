import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {FormBuilder, Validators} from "@angular/forms";
import {AlertService} from '../alert.service';

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
              private formBuilder: FormBuilder, private alertService: AlertService, private snackBar: MatSnackBar) { }

  ngOnInit() {
    this.alertService.getMessage().subscribe((message: any) => {
      if(message) {
        this.snackBar.open(message.text, "Dismiss", { duration: 3000 });
      }
    });
  }

  close() {
      this.dialogRef.close();
      return;
  }

  save() {
    if (this.data.application.metadata) {
      this.setAttributesFromJsonEditor();
    }
    this.data.next(this.data.application).subscribe(r => {
      this.dialogRef.close();
      this.data.refresher.refresh();
    }, err => {
      this.alertService.error(err);
    });
  }

  private setAttributesFromJsonEditor() {
    this.data.application.attributes = this.data.application.metadata;
    delete this.data.application.metadata;
  }
}
