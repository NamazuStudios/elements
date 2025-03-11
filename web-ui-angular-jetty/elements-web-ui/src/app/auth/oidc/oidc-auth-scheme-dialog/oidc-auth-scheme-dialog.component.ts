import { Component, Inject, OnInit } from "@angular/core";
import {
  MAT_DIALOG_DATA,
  MatDialogRef,
  MatDialog,
} from "@angular/material/dialog";
import { MatSnackBar } from "@angular/material/snack-bar";
import {FormBuilder, FormControl, FormGroup, Validators} from "@angular/forms";
import { AlertService } from "../../../alert.service";
import { COMMA, ENTER } from "@angular/cdk/keycodes";
import { OidcAuthScheme } from "../../../api/models/auth/auth-scheme-oidc";
import { CreateOidcAuthSchemeRequest } from "../../../api/models/auth/create-oidc-auth-scheme-request";
import { UpdateOidcAuthSchemeRequest } from "../../../api/models/auth/update-oidc-auth-scheme-request";
import { RegenerateKeysDialogComponent } from "../../keygen/regenerate-keys-dialog/regenerate-keys-dialog.component";
import { GeneratedKeysDialogComponent } from "../../keygen/generated-keys-dialog/generated-keys-dialog.component";
import {JwkViewModel} from "../../../models/jwk-view-model";
import {JWK} from "../../../api/models/auth/jwk";

@Component({
  selector: "app-oidc-auth-scheme-dialog",
  templateUrl: "./oidc-auth-scheme-dialog.component.html",
  styleUrls: ["./oidc-auth-scheme-dialog.component.css"],
})
export class OidcAuthSchemeDialogComponent implements OnInit {
  selectable = true;
  removable = true;
  addOnBlur = true;
  readonly separatorKeyCodes: number[] = [ENTER, COMMA];

  oidcAuthSchemeForm = this.formBuilder.group({
    issuer: [this.data.authScheme.issuer],
    keysUrl: [this.data.authScheme.keysUrl],
    mediaType: [this.data.authScheme.mediaType],
    keys: [this.data.authScheme.keys],
  });

  keysForm = this.formBuilder.group({
    alg: [],
    kid: [],
    kty: [],
    e: [],
    n: [],
    use: []
  })

  constructor(
    public dialog: MatDialog,
    public dialogRef: MatDialogRef<OidcAuthSchemeDialogComponent>,
    @Inject(MAT_DIALOG_DATA)
    public data: {
      isNew: boolean;
      authScheme: OidcAuthScheme;
      next: any;
      refresher: any;
    },
    private formBuilder: FormBuilder,
    private alertService: AlertService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit() {
    this.alertService.getMessage().subscribe((message: any) => {
      if (message) {
        this.snackBar.open(message.text, "Dismiss", { duration: 3000 });
      }
    });
  }

  get issuer(): string {
    return this.oidcAuthSchemeForm.get("issuer").value;
  }

  get keys(): Array<JWK> {
    return this.oidcAuthSchemeForm.get("keys").value;
  }

  get mediaType(): string {
    return this.oidcAuthSchemeForm.get("mediaType").value;
  }

  get keysUrl(): string {
    return this.oidcAuthSchemeForm.get("keysUrl").value;
  }

  addKey() {
    const newKey = new JwkViewModel();
    this.addControl(this.keysForm, this.keys.length, newKey);
    this.keys.push(newKey);
  }

  updateKey(key: JWK, index: number, property: string, event: any) {
    console.log("Updating key %o at index %i for event %o", key, index, event);
    key[property] = event.target.value;
    this.keys[index] = key;
  }

  deleteKey(index: number) {
    this.keys.splice(index, 1);
    this.removeControl(this.keysForm, index);
  }

  addControl(form: FormGroup, index: number, formEntry: JWK) {
    form.addControl('kid' + index, new FormControl(formEntry.kid, Validators.required));
    form.addControl('alg' + index, new FormControl(formEntry.alg, Validators.required));
    form.addControl('kty' + index, new FormControl(formEntry.kty, Validators.required));
    form.addControl('n' + index, new FormControl(formEntry.n, Validators.required));
    form.addControl('e' + index, new FormControl(formEntry.e, Validators.required));
    form.addControl('use' + index, new FormControl(formEntry.use, Validators.required));
  }

  removeControl(form: FormGroup, i: number) {
    form.removeControl('kid' + i);
    form.removeControl('alg' + i);
    form.removeControl('kty' + i);
    form.removeControl('n' + i);
    form.removeControl('e' + i);
    form.removeControl('use' + i);
  }

  getNewAuthSchemeData(): CreateOidcAuthSchemeRequest {
    const newAuthSchemeData: CreateOidcAuthSchemeRequest = {
      issuer: this.issuer,
      keys: this.keys,
      keysUrl: this.keysUrl,
      mediaType: this.mediaType,
    };

    return newAuthSchemeData;
  }

  getUpdateAuthSchemeData(): UpdateOidcAuthSchemeRequest {
    const updateAuthSchemeData: UpdateOidcAuthSchemeRequest = {
      id: this.data.authScheme.id,
      issuer: this.issuer,
      keys: this.keys,
      keysUrl: this.keysUrl,
      mediaType: this.mediaType,
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
