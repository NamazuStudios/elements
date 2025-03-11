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
import { Oauth2AuthScheme } from "../../../api/models/auth/auth-scheme-oauth2";
import { CreateOauth2AuthSchemeRequest } from "../../../api/models/auth/create-oauth2-auth-scheme-request";
import { UpdateOauth2AuthSchemeRequest } from "../../../api/models/auth/update-oauth2-auth-scheme-request";
import {Oauth2RequestKeyValue} from "../../../api/models/auth/oauth2-request-key-value";
import {Oauth2RequestKeyValueViewModel} from "../../../models/oauth2-request-key-value-view-model";

@Component({
  selector: "app-oauth2-auth-scheme-dialog",
  templateUrl: "./oauth2-auth-scheme-dialog.component.html",
  styleUrls: ["./oauth2-auth-scheme-dialog.component.css"],
})
export class Oauth2AuthSchemeDialogComponent implements OnInit {
  selectable = true;
  removable = true;
  addOnBlur = true;
  readonly separatorKeyCodes: number[] = [ENTER, COMMA];
  public publicKeyDisabled: boolean = false;

  // userLevels: UserLevel[] = [
  //   { key: "UNPRIVILEGED", description: "Unprivileged" },
  //   { key: "USER", description: "User" },
  //   { key: "SUPERUSER", description: "Superuser" },
  // ];
  //
  // algorithms: UserLevel[] = [
  //   { key: "RSA_256", description: "RSA 256" },
  //   { key: "RSA_384", description: "RSA 384" },
  //   { key: "RSA_512", description: "RSA 512" },
  //   { key: "ECDSA_256", description: "ECDSA 256" },
  //   { key: "ECDSA_384", description: "ECDSA 384" },
  //   { key: "ECDSA_512", description: "ECDSA 512" },
  // ];

  constructor(
    public dialog: MatDialog,
    public dialogRef: MatDialogRef<Oauth2AuthSchemeDialogComponent>,
    @Inject(MAT_DIALOG_DATA)
    public data: {
      isNew: boolean;
      authScheme: Oauth2AuthScheme;
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

  oauth2AuthSchemeForm = this.formBuilder.group({
    id: [this.data.authScheme.id],
    name: [this.data.authScheme.name],
    validationUrl: [this.data.authScheme.validationUrl],
    responseIdMapping: [this.data.authScheme.responseIdMapping],
    headers: [this.data.authScheme.headers],
    params: [this.data.authScheme.params],
  });

  headersForm = this.formBuilder.group({
    key: '',
    value: '',
    fromClient: false,
  })

  paramsForm = this.formBuilder.group({
    key: '',
    value: '',
    fromClient: false,
  })

  get id(): string {
    return this.oauth2AuthSchemeForm.get("id").value;
  }

  get name(): string {
    return this.oauth2AuthSchemeForm.get("name").value;
  }

  get validationUrl(): string {
    return this.oauth2AuthSchemeForm.get("validationUrl").value;
  }

  get headers(): Array<Oauth2RequestKeyValue> {
    return this.oauth2AuthSchemeForm.get("headers").value;
  }

  get params(): Array<Oauth2RequestKeyValue> {
    return this.oauth2AuthSchemeForm.get("params").value;
  }

  get responseIdMapping(): string {
    return this.oauth2AuthSchemeForm.get("responseIdMapping").value;
  }

  getIsChecked(okv: Oauth2RequestKeyValue) : boolean {
    return okv.fromClient;
  }

  updateHeader(header: Oauth2RequestKeyValue, index: number, property: string, event: any) {
    if (property == "fromClient") {
      console.log("Updating checkbox for header %o at index %i for event %o to %s", header, index, event, event.checked);
      header.fromClient = event.checked;
    } else {
      console.log("Updating header %o at index %i for event %o", header, index, event);
      header[property] = event.target.value;
      this.headers[index] = header;
    }
  }

  addHeader() {
    const newHeader = new Oauth2RequestKeyValueViewModel();
    this.addControl(this.headersForm, this.headers.length, newHeader);
    this.headers.push(newHeader);
  }

  deleteHeader(index: number) {
    this.headers.splice(index, 1);
    this.removeControl(this.headersForm, index);
  }

  updateParam(param: Oauth2RequestKeyValue, index: number, property: string, event: any) {
    if (property == "fromClient") {
      console.log("Updating checkbox for param %o at index %i for event %o to %s", param, index, event, event.checked);
      param.fromClient = event.checked;
    } else {
      console.log("Updating param %o at index %i for event %o", param, index, event);
      param[property] = event.target.value;
      this.params[index] = param;
    }
  }

  addParam() {
    const newParam = new Oauth2RequestKeyValueViewModel();
    this.addControl(this.paramsForm, this.params.length, newParam);
    this.params.push(newParam);
  }

  deleteParam(index: number) {
    this.params.splice(index, 1);
    this.removeControl(this.paramsForm, index);
  }

  addControl(form: FormGroup, index: number, formEntry: Oauth2RequestKeyValueViewModel) {
    form.addControl('key' + index, new FormControl(formEntry.key, Validators.required));
    form.addControl('value' + index, new FormControl(formEntry.value, Validators.required));
    form.addControl('fromClient' + index, new FormControl(formEntry.fromClient, Validators.required));
  }

  removeControl(form: FormGroup, i: number) {
    form.removeControl('key' + i);
    form.removeControl('value' + i);
    form.removeControl('fromClient' + i);
  }

  getNewAuthSchemeData(): CreateOauth2AuthSchemeRequest {
    let newAuthSchemeData: CreateOauth2AuthSchemeRequest = {
      name: this.name,
      validationUrl: this.validationUrl !== "" ? this.validationUrl : null,
      responseIdMapping: this.responseIdMapping,
      params: this.params,
      headers: this.headers,
    };

    console.log("Got new auth scheme data ", newAuthSchemeData);
    return newAuthSchemeData;
  }

  getUpdateAuthSchemeData(): UpdateOauth2AuthSchemeRequest {
    let updateAuthSchemeData: UpdateOauth2AuthSchemeRequest = {
      id: this.id,
      name: this.name,
      validationUrl: this.validationUrl !== "" ? this.validationUrl : null,
      responseIdMapping: this.responseIdMapping,
      params: this.params,
      headers: this.headers,
    };

    console.log("Got update auth scheme data ", updateAuthSchemeData);
    return updateAuthSchemeData;
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
