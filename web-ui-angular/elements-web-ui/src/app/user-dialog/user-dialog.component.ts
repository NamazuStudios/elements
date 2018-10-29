import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material";
import {AbstractControl, FormBuilder, FormControl, ValidatorFn, Validators} from "@angular/forms";

@Component({
  selector: 'app-user-dialog',
  templateUrl: './user-dialog.component.html',
  styleUrls: ['./user-dialog.component.css']
})
export class UserDialogComponent implements OnInit {

  userForm = this.formBuilder.group({
    id: [ this.data.user.id, [ Validators.required, Validators.pattern('^[a-zA-Z0-9]+$') ]],
    email: [ this.data.user.description, [ Validators.required, Validators.email ]],
    password: [ this.data.user.password, Validators.required ],
    passwordConfirmation: [ this.data.user.password, [ Validators.required, this.passwordMatchValidator ] ],
  });

  constructor(public dialogRef: MatDialogRef<UserDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any,
              private formBuilder: FormBuilder) { }

  ngOnInit() {
  }

  passwordMatchValidator(c: AbstractControl) : { [key: string]: boolean } | null {
    if(c.parent.get('password').value != c.value) {
      return { passwordMatch: true };
    }

    return null;
  }
}
