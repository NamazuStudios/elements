import {Component, Inject, OnInit, ViewChild} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {FormBuilder, Validators} from '@angular/forms';
import {AlertService} from '../alert.service';
import {ApplicationAttributesComponent} from '../application-attributes/application-attributes.component';

@Component({
  selector: 'app-application-dialog',
  templateUrl: './application-dialog.component.html',
  styleUrls: ['./application-dialog.component.css']
})
export class ApplicationDialogComponent implements OnInit {

  @ViewChild(ApplicationAttributesComponent) attributesCard: ApplicationAttributesComponent;

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
      if (message) {
        this.snackBar.open(message.text, 'Dismiss', { duration: 3000 });
      }
    });
  }

  close() {
      this.dialogRef.close();
      return;
  }

  save() {
    this.attributesCard.validateAttributes(true);
    this.data.next(this.data.application).subscribe(r => {
      this.dialogRef.close();
      this.data.refresher.refresh();
    }, err => {
      this.alertService.error(err);
    });
  }
}
