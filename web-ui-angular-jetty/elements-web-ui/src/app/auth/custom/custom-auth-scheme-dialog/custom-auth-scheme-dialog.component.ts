import { Component, Inject, OnInit } from "@angular/core";
import {
  MAT_DIALOG_DATA,
  MatDialogRef,
  MatDialog,
} from "@angular/material/dialog";
import { MatSnackBar } from "@angular/material/snack-bar";
import { FormBuilder } from "@angular/forms";
import { AlertService } from "../../../alert.service";
import { UsersService } from "../../../api/services";
import { MatChipInputEvent } from "@angular/material/chips";
import { COMMA, ENTER } from "@angular/cdk/keycodes";
import { CustomAuthSchemesService } from "../../../api/services/auth/custom-auth-schemes.service";
import { CustomAuthScheme } from "../../../api/models/auth/auth-scheme-custom";
import { CreateCustomAuthSchemeRequest } from "../../../api/models/auth/create-custom-auth-scheme-request";
import { UpdateCustomAuthSchemeRequest } from "../../../api/models/auth/update-custom-auth-scheme-request";
import { UserLevel } from "../../../users/user-dialog/user-dialog.component";
import { RegenerateKeysDialogComponent } from "../../keygen/regenerate-keys-dialog/regenerate-keys-dialog.component";
import { GeneratedKeysDialogComponent } from "../../keygen/generated-keys-dialog/generated-keys-dialog.component";

@Component({
  selector: "app-custom-auth-scheme-dialog",
  templateUrl: "./custom-auth-scheme-dialog.component.html",
  styleUrls: ["./custom-auth-scheme-dialog.component.css"],
})
export class CustomAuthSchemeDialogComponent implements OnInit {
  selectable = true;
  removable = true;
  addOnBlur = true;
  readonly separatorKeyCodes: number[] = [ENTER, COMMA];
  public publicKeyDisabled: boolean = false;

  userLevels: UserLevel[] = [
    { key: "UNPRIVILEGED", description: "Unprivileged" },
    { key: "USER", description: "User" },
    { key: "SUPERUSER", description: "Superuser" },
  ];

  algorithms: UserLevel[] = [
    { key: "RSA_256", description: "RSA 256" },
    { key: "RSA_384", description: "RSA 384" },
    { key: "RSA_512", description: "RSA 512" },
    { key: "ECDSA_256", description: "ECDSA 256" },
    { key: "ECDSA_384", description: "ECDSA 384" },
    { key: "ECDSA_512", description: "ECDSA 512" },
  ];

  customAuthSchemeForm = this.formBuilder.group({
    audience: [this.data.authScheme.audience],
    regenerate: [false],
    publicKey: [
      {
        value: this.data.authScheme.publicKey,
        disabled: this.publicKeyDisabled,
      },
    ],
    algorithm: [this.data.authScheme.algorithm],
    userLevel: [this.data.authScheme.userLevel],
    tags: [[]],
    allowedIssuers: [[]],
  });

  get audience(): string {
    return this.customAuthSchemeForm.get("audience").value;
  }

  get regenerate(): boolean {
    return this.customAuthSchemeForm.get("regenerate").value;
  }

  get publicKey(): string {
    return this.customAuthSchemeForm.get("publicKey").value;
  }

  get algorithm(): CustomAuthScheme["algorithm"] {
    return this.customAuthSchemeForm.get("algorithm").value;
  }

  get userLevel(): CustomAuthScheme["userLevel"] {
    return this.customAuthSchemeForm.get("userLevel").value;
  }

  constructor(
    public dialogRef: MatDialogRef<CustomAuthSchemeDialogComponent>,
    @Inject(MAT_DIALOG_DATA)
    public data: {
      isNew: boolean;
      authScheme: CustomAuthScheme;
      next: any;
      refresher: any;
    },
    // @Inject(MAT_DIALOG_DATA) public data: any,
    private formBuilder: FormBuilder,
    private alertService: AlertService,
    private authSchemesService: CustomAuthSchemesService,
    private usersService: UsersService,
    public dialog: MatDialog,

    private snackBar: MatSnackBar
  ) {}

  ngOnInit() {
    this.alertService.getMessage().subscribe((message: any) => {
      if (message) {
        this.snackBar.open(message.text, "Dismiss", { duration: 3000 });
      }
    });
  }

  addTag(event: MatChipInputEvent): void {
    const input = event.input;
    const value = event.value;

    if ((value || "").trim()) {
      if (!this.data.authScheme.tags) {
        this.data.authScheme.tags = [];
      }
      this.data.authScheme.tags.push(value);
    }

    if (input) {
      input.value = "";
    }
  }

  removeTag(tag: string): void {
    const index = this.data.authScheme.tags.indexOf(tag);

    if (index >= 0) {
      this.data.authScheme.tags.splice(index, 1);
    }
  }

  addAllowedIssuers(event: MatChipInputEvent): void {
    const input = event.input;
    const value = event.value;

    if ((value || "").trim()) {
      if (!this.data.authScheme.allowedIssuers) {
        this.data.authScheme.allowedIssuers = [];
      }
      this.data.authScheme.allowedIssuers.push(value);
    }

    if (input) {
      input.value = "";
    }
  }

  removeAllowedIssuers(allowedIssuers: string): void {
    const index = this.data.authScheme.allowedIssuers.indexOf(allowedIssuers);

    if (index >= 0) {
      this.data.authScheme.allowedIssuers.splice(index, 1);
    }
  }

  getNewAuthSchemeData(): CreateCustomAuthSchemeRequest {
    let newAuthSchemeData: CreateCustomAuthSchemeRequest = {
      audience: this.audience,
      publicKey: this.publicKey !== "" ? this.publicKey : null,
      algorithm: this.algorithm,
      userLevel: this.userLevel,
      tags: this.data.authScheme.tags,
      allowedIssuers: this.data.authScheme.allowedIssuers,
    };

    return newAuthSchemeData;
  }

  getUpdateAuthSchemeData(): UpdateCustomAuthSchemeRequest {
    let updateAuthSchemeData: UpdateCustomAuthSchemeRequest = {
      audience: this.audience,
      regenerate: this.regenerate,
      publicKey: this.regenerate ? null : this.publicKey,
      algorithm: this.algorithm,
      userLevel: this.userLevel,
      tags: this.data.authScheme.tags,
      allowedIssuers: this.data.authScheme.allowedIssuers,
    };

    return updateAuthSchemeData;
  }

  showDialog(next: (regenerate: boolean) => void) {
    this.dialog.open(RegenerateKeysDialogComponent, {
      width: "600px",
      data: {
        next: next,
      },
    });
  }

  showKeysDialog(publicKey: string, privateKey: string) {
    this.dialog.open(GeneratedKeysDialogComponent, {
      width: "600px",
      data: {
        publicKey,
        privateKey,
      },
    });
  }

  regenerateKeys() {
    if (!this.customAuthSchemeForm.get("regenerate").value && !this.data.isNew) {
      this.showDialog((regenerate: boolean) => {
        this.customAuthSchemeForm.get("regenerate").setValue(regenerate);

          if(regenerate){
            this.customAuthSchemeForm.get("publicKey").disable();
            this.customAuthSchemeForm.get("publicKey").patchValue("");
          } else {
            this.customAuthSchemeForm.get("publicKey").enable();
          }
      });
    }

      if(!this.customAuthSchemeForm.get("regenerate").value && this.data.isNew){
        this.customAuthSchemeForm.get("publicKey").disable();
        this.customAuthSchemeForm.get("publicKey").patchValue("");
      } else {
        this.customAuthSchemeForm.get("publicKey").enable();
      }
  }

  close(saveChanges?: boolean) {
    if (!saveChanges) {
      this.dialogRef.close();
      return;
    }

    let authSchemeData;
    this.data.isNew
      ? (authSchemeData = this.getNewAuthSchemeData())
      : (authSchemeData = this.getUpdateAuthSchemeData());

    this.data.next(authSchemeData).subscribe(
      (authResponse) => {
        if (authResponse.privateKey) {
          this.showKeysDialog(authResponse.publicKey, authResponse.privateKey);
        }

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
}
