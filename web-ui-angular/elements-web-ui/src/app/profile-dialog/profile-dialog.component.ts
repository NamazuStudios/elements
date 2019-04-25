import {Component, Inject, OnInit, ViewChild} from '@angular/core';
import {JsonEditorCardComponent} from '../json-editor-card/json-editor-card.component';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef, MatSnackBar} from '@angular/material';
import {FormBuilder, Validators} from '@angular/forms';
import {AlertService} from '../alert.service';
import {Application} from '../api/models/application';
import {User} from '../api/models/user';
import {UserDialogComponent} from '../user-dialog/user-dialog.component';
import {UsersService} from '../api/services/users.service';

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

  constructor(public dialogRef: MatDialogRef<ProfileDialogComponent>, public dialog: MatDialog,
              @Inject(MAT_DIALOG_DATA) public data: any, private usersService: UsersService,
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

  showEditUserDialog(user: User) {
    this.dialog.open(UserDialogComponent, {
      width: "500px",
      data: {
        isNew: false, user: user, refresher: this, next: result => {
          const password = result.password;
          delete result.passwordConfirmation;
          return this.usersService.updateUser({name: user.name, password: password, body: result});
        }
      }
    });
  }

  compareApps(app1, app2) {
    return app1.name == app2.name;
  }

}
