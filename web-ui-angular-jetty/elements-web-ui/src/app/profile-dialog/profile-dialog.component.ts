import {Component, Inject, OnInit, ViewChild} from '@angular/core';
import {JsonEditorCardComponent} from '../json-editor-card/json-editor-card.component';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {FormBuilder, Validators} from '@angular/forms';
import {AlertService} from '../alert.service';
import {Application} from '../api/models/application';
import {User} from '../api/models/user';
import {UserDialogComponent} from '../user-dialog/user-dialog.component';
import {UsersService} from '../api/services/users.service';
import { map } from 'rxjs/operators';
import {UserSelectDialogComponent} from '../user-select-dialog/user-select-dialog.component';
import { InventoryDialogComponent } from '../inventory-dialog/inventory-dialog.component';

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
    application: [{value: this.data.profile.application, disabled: !this.data.isNew}, [Validators.required]],
    userName: [{value: this.data.profile.user ? this.data.profile.user.name : undefined, disabled: true}, [Validators.required]],
    userEmail: [{value: this.data.profile.user ? this.data.profile.user.email : undefined, disabled: true}, [Validators.required]],
    userFacebook: [{value: this.data.profile.user ? this.data.profile.user.facebookId : undefined, disabled : true}]
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
    if (this.data.profile.user !== undefined) {
      formData.user = this.data.profile.user;
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
      width: '700px',
      data: {
        isNew: false, user: user, next: result => {
          delete result.passwordConfirmation;
          delete result.id;
          if(result.password === "") { delete result.password}
          return this.usersService.updateUser({name: user.id, body: result}).pipe(
            map(r => {
              this.data.profile.user = r;
            })
          );
        }
      }
    });
  }

  showSelectUserDialog() {
    this.dialog.open(UserSelectDialogComponent, {
      width: '700px',
      data: {
        next: result => {
          this.data.profile.user = result;
        }
      }
    });
  }

  showInventoryDialog() {
    this.dialog.open(InventoryDialogComponent, {
      width: '800px',
      data: {
        user: this.data.profile.user
      }
    });
  }

  compareApps(app1, app2) {
    if (app1 === undefined || app2 === undefined) { return false; }
    if (app1 == null && app2 == null) { return true; }
    return app1.name === app2.name;
  }

  parseDate(timestamp: number) {
    const date = new Date(timestamp);

    return date;
  }

}
