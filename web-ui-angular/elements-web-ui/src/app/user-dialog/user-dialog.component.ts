import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef, MatSnackBar} from '@angular/material';
import {AbstractControl, FormBuilder, FormControl, ValidatorFn, Validators} from "@angular/forms";
import {AlertService} from '../alert.service';

export interface UserLevel {
  key: string;
  description: string;
}

@Component({
  selector: 'app-user-dialog',
  templateUrl: './user-dialog.component.html',
  styleUrls: ['./user-dialog.component.css']
})
export class UserDialogComponent implements OnInit {

  userLevels: UserLevel[] = [
    { key: "UNPRIVILEGED", description: "Unprivileged" },
    { key: "USER", description: "User" },
    { key: "SUPERUSER", description: "Superuser" }
  ];

  userForm = this.formBuilder.group({
    id: [ this.data.user.id ],
    name: [ this.data.user.name, [ Validators.required, Validators.pattern('^[a-zA-Z0-9]+$') ]],
    email: [ this.data.user.email, [ Validators.required, Validators.email ]],
    password: [ '' ],
    passwordConfirmation: [ '', this.passwordMatchValidator ],
    level: [ this.data.user.level, Validators.required ]
  });

  constructor(public dialogRef: MatDialogRef<UserDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any,
              private formBuilder: FormBuilder, private alertService: AlertService, private snackBar: MatSnackBar) { }

  ngOnInit() {
    if(this.data.isNew) {
      this.userForm.get("password").setValidators(Validators.required);
    }
    this.alertSubscription = this.alertService.getMessage().subscribe((message: any) => {
      if(message) {
        this.snackBar.open(message.text, "Dismiss", { duration: 3000 });
      }
    });
  }

  close(res: any) {
    if (!res) {
      this.dialogRef.close();
      return;
    }

    this.data.next(res).subscribe(r => {
      this.dialogRef.close();
      this.data.refresher.refresh();
    }, err => {
      this.alertService.error(err);
    });
  }

  passwordMatchValidator(c: AbstractControl) : { [key: string]: boolean } | null {
    let parent = c.parent;

    if(!parent)
      return null;

    if(parent.get('password').value != c.value) {
      return { passwordMatch: true };
    }

    return null;
  }
}
