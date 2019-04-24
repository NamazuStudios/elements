import {Component, Inject, OnInit, ViewChild} from '@angular/core';
import {JsonEditorCardComponent} from '../json-editor-card/json-editor-card.component';
import {MAT_DIALOG_DATA, MatDialogRef, MatSnackBar} from '@angular/material';
import {FormBuilder, Validators} from '@angular/forms';
import {AlertService} from '../alert.service';
import {Application} from '../api/models/application';

@Component({
  selector: 'app-profile-dialog',
  templateUrl: './profile-dialog.component.html',
  styleUrls: ['./profile-dialog.component.css']
})
export class ProfileDialogComponent implements OnInit {

  @ViewChild(JsonEditorCardComponent) editorCard: JsonEditorCardComponent;

  originalMetadata = JSON.parse(JSON.stringify(this.data.profile.metadata || {}));

  profileForm = this.formBuilder.group({
    displayName: [this.data.profile.displayName, [Validators.required]],
    imageUrl: [this.data.profile.imageUrl],
    application: [this.data.profile.application]
  });

  constructor(public dialogRef: MatDialogRef<ProfileDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any,
              private formBuilder: FormBuilder, private alertService: AlertService, private snackBar: MatSnackBar) {
  }

  ngOnInit() {
    this.alertService.getMessage().subscribe((message: any) => {
      if (message) {
        this.snackBar.open(message.text, 'Dismiss', { duration: 3000 });
      }
    });
  }

  close(saveChanges?: boolean): void {
    if (!saveChanges) {
      this.data.profile.metadata = this.originalMetadata;
      this.dialogRef.close();
      return;
    }

    this.editorCard.validateMetadata(true);

    const formData = this.profileForm.value;
    if (this.data.profile.metadata !== undefined) {
      formData.metadata = this.data.profile.metadata;
    }

    this.data.next(formData).subscribe(r => {
      this.dialogRef.close();
      this.data.refresher.refresh();
    }, err => {
      this.alertService.error(err);
    });
  }

  compareApps(app1, app2) {
    return app1.name == app2.name;
  }

}
