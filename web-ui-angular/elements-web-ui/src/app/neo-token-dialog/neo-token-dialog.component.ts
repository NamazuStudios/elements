import { Component, Inject, OnInit } from "@angular/core";
import {
  MAT_DIALOG_DATA,
  MatDialogRef,
  MatDialog,
} from "@angular/material/dialog";
import { MatSnackBar } from "@angular/material/snack-bar";
import { FormBuilder, Validators } from "@angular/forms";
import { AlertService } from "../alert.service";
import { NeoWalletsService, UsersService } from "../api/services";

import { Token } from "../api/models/blockchain/token";
import { UpdateNeoTokenRequest } from "../api/models/blockchain/update-neo-token-request";
import { CreateNeoTokenRequest } from "../api/models/blockchain/create-neo-token-request";
import { MatChipInputEvent } from "@angular/material/chips";
import { COMMA, ENTER } from "@angular/cdk/keycodes";
import { NeoTokenViewModel } from "../models/blockchain/neo-token-view-model";
import { NeoToken } from "../api/models/blockchain/neo-token";

export interface OptionType {
  key: string;
  label: string;
  toolTip: string;
}

@Component({
  selector: "app-neo-token-dialog",
  templateUrl: "./neo-token-dialog.component.html",
  styleUrls: ["./neo-token-dialog.component.css"],
})
export class NeoTokenDialogComponent implements OnInit {
  selectable = true;
  removable = true;
  addOnBlur = true;
  readonly separatorKeyCodes: number[] = [ENTER, COMMA];

  transferOptionType: OptionType[] = [
    { key: "none", label: "None", toolTip: "Cannot be transferred." },
    {
      key: "resale_only",
      label: "Resale only",
      toolTip: "Can be resold, but not traded.",
    },
    {
      key: "trades_only",
      label: "Trades only",
      toolTip: "Can be traded, but not resold.",
    },
    {
      key: "resale_and_trades",
      label: "Resale and Trades",
      toolTip: "Can be either resold or traded.",
    },
  ];

  tokenForm = this.formBuilder.group({

    owner: [this.data.neoToken.token.owner], // DONE <>
    name: [this.data.neoToken.token.name, [Validators.required]], // DONE <>
    description: [this.data.neoToken.token.description],
    tags: [[]], // DONE <>
    totalSupply: [this.data.neoToken?.token?.totalSupply], // DONE <>
    accessOption: [this.data.neoToken.token.accessOption, [Validators.required]], // DONE <>
    previewUrls: [this.data.neoToken?.token?.previewUrls], // DONE <>
    assetUrls: [this.data.neoToken?.token?.assetUrls], // DONE <>

    // ownership: // TODO: ADD TO FORM (optional for now) <<<<<<<<<<<<<<<<<<<<<<<<

    transferOptions: [
      this.data.neoToken?.token?.transferOptions,
      [Validators.required],
    ], // DONE <>
    revocable: [this.data.neoToken?.token?.revocable, ], // DONE <>

    expiry: [this.data.neoToken?.token?.expiry, ], // TODO: link this to form <<<<<<<<<<<<<<<<

    renewable: [this.data.neoToken?.token?.renewable, ], // DONE <>
    listed: [this.data.neoToken?.listed], // DONE <>

    contractId: [this.data.neoToken?.contractId] // TODO: link this to form later.... <<<<<<<<<<<
 
    //  metadata: [this.data.neoToken?.metadata, [Validators.required]], // TODO: ADD TO FORM <<<<<<<<<

  });

  get owner(): string {
    return this.tokenForm.get("owner").value;
  }
  
  get name(): string {
    return this.tokenForm.get("name").value;
  }

  get description(): string {
    return this.tokenForm.get("description").value;
  }

  // tags

  get totalSupply(): number {
    return this.tokenForm.get("totalSupply").value;
  }

  get accessOption(): Token["accessOption"] {
    return this.tokenForm.get("accessOption").value;
  }

  get previewUrls(): Array<string> {
    return this.tokenForm.get("previewUrls").value;
  }
  get assetUrls(): Array<string> {
    return this.tokenForm.get("assetUrls").value;
  }

  // ownership - TODO: need to finish this

  get transferOptions(): Token["transferOptions"] {
    return this.tokenForm.get("transferOptions").value;
  }

  get revocable(): boolean {
    return this.tokenForm.get("revocable").value;
  }

  // expiry - TODO: need to finish this

  get renewable(): boolean {
    return this.tokenForm.get("renewable").value;
  }

  get listed(): boolean {
    return this.tokenForm.get("listed").value;
  }

  get contractId(): string {
    return this.tokenForm.get("contractId").value;
  }

  constructor(
    public dialogRef: MatDialogRef<NeoTokenDialogComponent>,
    @Inject(MAT_DIALOG_DATA)
    public data: {
      isNew: boolean;
      neoToken: NeoToken;
      next: any;
      refresher: any;
    },
    // @Inject(MAT_DIALOG_DATA) public data: any,
    private formBuilder: FormBuilder,
    private alertService: AlertService,
    private neoWalletsService: NeoWalletsService,
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
      if (!this.data.neoToken.token.tags) {
        this.data.neoToken.token.tags = [];
      }
      this.data.neoToken.token.tags.push(value);
    }

    if (input) {
      input.value = "";
    }
  }

  remove(tag: string): void {
    const index = this.data.neoToken.token.tags.indexOf(tag);

    if (index >= 0) {
      this.data.neoToken.token.tags.splice(index, 1);
    }
  }

  addPreviewUrl(newData: string) {
    if (!this.data.neoToken.token.previewUrls) {
      this.data.neoToken.token.previewUrls = [];
    }
    this.data.neoToken.token.previewUrls.push(newData);
  }

  editPreviewUrl(newData: string, index: number) {
    this.data.neoToken.token.previewUrls[index] = newData;
  }

  removePreviewUrlAtIndex(index: number) {
    this.data.neoToken.token.previewUrls.splice(index, 1);
  }

  addAssetUrl(newData: string) {
    if (!this.data.neoToken.token.assetUrls) {
      this.data.neoToken.token.assetUrls = [];
    }
    this.data.neoToken.token.assetUrls.push(newData);
  }

  editAssetUrl(newData: string, index: number) {
    this.data.neoToken.token.assetUrls[index] = newData;
  }

  removeAssetUrlAtIndex(index: number) {
    this.data.neoToken.token.assetUrls.splice(index, 1);
  }

  getNewTokenData(): CreateNeoTokenRequest {
    let newTokenData: CreateNeoTokenRequest = {
      token: {
        owner: this.owner, 
        name: this.name,
        description: this.description,
        tags: this.data.neoToken.token.tags,
        totalSupply: this.totalSupply,
        accessOption: this.accessOption,
        previewUrls: this.previewUrls,
        assetUrls: this.assetUrls,
        ownership: null, // TODO link this to the form <<<<<<<<<<<<<<<<<<<<
        transferOptions: this.transferOptions,
        revocable: this.revocable, 
        expiry: 0, // TODO link this to form <<<<<<<<<<<<<<<<<<<<<<<<<
        renewable: this.renewable, 
        metadata: JSON.parse(
          JSON.stringify(this.data.neoToken.token.metadata))
      },
      listed: this.listed,
      contractId: this.contractId
    };

    return newTokenData;
  }

  getUpdateTokenData(): UpdateNeoTokenRequest {
    let updateWalletData: UpdateNeoTokenRequest = {
      token: {
        owner: this.owner, 
        name: this.name,
        description: this.description,
        tags: this.data.neoToken.token.tags,
        totalSupply: this.totalSupply, 
        accessOption: this.accessOption,
        previewUrls: this.previewUrls,
        assetUrls: this.assetUrls,
        ownership: null, // TODO link this to the form!!!!!!!
        transferOptions: this.transferOptions,
        revocable: this.revocable, 
        expiry: 0, // TODO link this to form
        renewable: this.renewable, 
        metadata: JSON.parse(
          JSON.stringify(this.data.neoToken.token.metadata))
      },
      listed: this.listed,
      contractId: this.contractId
    };

    return updateWalletData;
  }

  close(saveChanges?: boolean) {
    if (!saveChanges) {
      this.dialogRef.close();
      return;
    }

    let tokenData;
    this.data.isNew
      ? (tokenData = this.getNewTokenData())
      : (tokenData = this.getUpdateTokenData());

    this.data.next(tokenData).subscribe(
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
}
