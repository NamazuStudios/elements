import { Component, Inject, OnInit } from "@angular/core";
import { MAT_DIALOG_DATA, MatDialogRef } from "@angular/material/dialog";
import { MatSnackBar } from "@angular/material/snack-bar";
import {
  AbstractControl,
  FormBuilder,
  ValidatorFn,
  Validators,
} from "@angular/forms";
import { AlertService } from "../../alert.service";

export interface UserLevel {
  key: string;
  description: string;
}

@Component({
  selector: "app-user-dialog",
  templateUrl: "./user-dialog.component.html",
  styleUrls: ["./user-dialog.component.css"],
})
export class UserDialogComponent implements OnInit {
  hidePassword1 = true;
  hidePassword2 = true;

  userLevels: UserLevel[] = [
    { key: "UNPRIVILEGED", description: "Unprivileged" },
    { key: "USER", description: "User" },
    { key: "SUPERUSER", description: "Superuser" },
  ];

  userForm = this.formBuilder.group({
    id: [this.data.user.id],
    name: [
      this.data.user.name,
      [Validators.required, Validators.pattern("^\\S+$")],
    ],
    email: [this.data.user.email, [Validators.required, Validators.email]],
    password: ["", [Validators.pattern("^\\S+$")]],
    passwordConfirmation: ["", this.getPasswordMatchValidator("password")],
    level: [this.data.user.level, Validators.required],
    linkedAccounts: [ {value: "\n", disabled: true} ]
  });

  constructor(
    public dialogRef: MatDialogRef<UserDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private formBuilder: FormBuilder,
    private alertService: AlertService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit() {
    if (this.data.isNew) {
      this.userForm.get("password").setValidators(Validators.required);
    }
    this.alertService.getMessage().subscribe((message: any) => {
      if (message) {
        this.snackBar.open(message.text, "Dismiss", { duration: 3000 });
      }
    });
  }

  close(res?: any) {
    if (!res) {
      this.dialogRef.close();
      return;
    }

    this.data.next(res).subscribe(
      (r) => {
        this.dialogRef.close();
        if (this.data.refresher) {
          this.data.refresher.refresh();
        }
      },
      (err) => {
        this.alertService.error(err);
      }
    );
  }

  getPasswordMatchValidator(confirmPasswordControlName: string): ValidatorFn {
    return (control: AbstractControl): { [key: string]: boolean } | null => {
      let parent = control.parent;
      if (!parent) return null;

      if (parent.get(confirmPasswordControlName).value != control.value) {
        return { passwordMatch: true };
      }

      return null;
    };
  }
}
