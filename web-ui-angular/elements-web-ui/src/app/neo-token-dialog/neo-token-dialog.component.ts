import { Component, Inject, OnInit, ViewChild } from "@angular/core";
import {
  MAT_DIALOG_DATA,
  MatDialogRef,
  MatDialog,
} from "@angular/material/dialog";
import { MatSnackBar } from "@angular/material/snack-bar";
import {
  AbstractControl,
  FormBuilder,
  ValidatorFn,
  Validators,
} from "@angular/forms";
import { AlertService } from "../alert.service";
import { NeoWalletsService, UsersService } from "../api/services";

import { Token } from "../api/models/blockchain/token";
import { UpdateNeoTokenRequest } from "../api/models/blockchain/update-neo-token-request";
import { CreateNeoTokenRequest } from "../api/models/blockchain/create-neo-token-request";
import { MatChipInputEvent } from "@angular/material/chips";
import { COMMA, ENTER } from "@angular/cdk/keycodes";
import { NeoTokenViewModel } from "../models/blockchain/neo-token-view-model";
import { NeoToken } from "../api/models/blockchain/neo-token";
import { JsonEditorCardComponent } from "../json-editor-card/json-editor-card.component";
import { NeoSmartContract } from "../api/models/blockchain/neo-smart-contract";
import { NeoSmartContractsDialogComponent } from "../neo-smart-contracts-dialog/neo-smart-contracts-dialog.component";
import { NeoSmartContractSelectDialogComponent } from "../neo-smart-contract-select-dialog/neo-smart-contract-select-dialog.component";
import { StakeHolder } from "../api/models/blockchain/stake-holder";
import { NeoSmartContractsService } from "../api/services/blockchain/neo-smart-contracts.service";
import { WalletSelectDialogComponent } from "../wallet-select-dialog/wallet-select-dialog.component";
import { NeoWallet } from "../api/models/blockchain/neo-wallet";

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
  @ViewChild(JsonEditorCardComponent) editorCard: JsonEditorCardComponent;

  currentSmartContract: NeoSmartContract;
  currentWallet: NeoWallet;
  shareView: boolean = true;

  originalMetadata = JSON.parse(
    JSON.stringify(this.data.neoToken.token.metadata || {})
  );

  originalOwnership = JSON.parse(
    JSON.stringify(this.data.neoToken.token.ownership)
  );

  selectable = true;
  removable = true;
  addOnBlur = true;
  readonly separatorKeyCodes: number[] = [ENTER, COMMA];
  readonly capitalizationShares: number = 10000;

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

  tokenForm = this.formBuilder.group(
    {
      voting: [false],
      existingVoting: [],
      capitalization: [{ value: this.capitalizationShares, disabled: true }],
      owner: [{ value: this.data.neoToken?.token?.owner, disabled: true }],
      name: [this.data.neoToken?.token?.name, [Validators.required]],
      description: [this.data.neoToken?.token?.description],
      tags: [[]],
      totalSupply: [this.data.neoToken?.token?.totalSupply],
      accessOption: [
        this.data.neoToken?.token?.accessOption,
        [Validators.required],
      ],
      previewUrls: [this.data.neoToken?.token?.previewUrls],
      assetUrls: [this.data.neoToken?.token?.assetUrls],
      transferOptions: [
        this.data.neoToken?.token?.transferOptions,
        [Validators.required],
      ],
      revocable: [this.data.neoToken?.token?.revocable],

      expiry: [this.data.neoToken?.token?.expiry], // TODO: link this to form <<<<<<<<<<<<<<<<

      renewable: [this.data.neoToken?.token?.renewable],
      listed: [this.data.neoToken?.listed],

      contractId: [{ value: "", disabled: true }],
    },
    {
      validator: this.getCapitalizationSharesValidator(
        this.data.neoToken?.token?.ownership?.stakeHolders,
        this.capitalizationShares
      ),
    }
  );

  get owner(): string {
    return this.tokenForm.get("owner").value;
  }

  get name(): string {
    return this.tokenForm.get("name").value;
  }

  get voting(): boolean {
    return this.tokenForm.get("voting").value;
  }

  get votingControl(): AbstractControl {
    return this.tokenForm.get("voting");
  }

  get existingVoting(): boolean {
    return this.tokenForm.get("existingVoting").value;
  }

  get description(): string {
    return this.tokenForm.get("description").value;
  }

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
    private neoSmartContractService: NeoSmartContractsService,
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

    this.neoSmartContractService
      .getNeoSmartContract(this.data.neoToken.contractId)
      .subscribe((neoContract) => {
        this.tokenForm.get("contractId").patchValue(neoContract.displayName);
        this.currentSmartContract = JSON.parse(JSON.stringify(neoContract));
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

  addStakeHolder(owner: string, shares: number) {
    if (!this.data.neoToken.token.ownership.stakeHolders) {
      this.data.neoToken.token.ownership.stakeHolders = [];
    }
    this.data.neoToken.token.ownership.stakeHolders.push({
      owner: owner,
      voting: this.voting,
      shares: shares,
    });
  }

  removeStakeHolderAtIndex(index: number) {
    this.data.neoToken.token.ownership.stakeHolders.splice(index, 1);
  }

  addPreviewUrl(newData: string) {
    if (!this.data.neoToken.token.previewUrls) {
      this.data.neoToken.token.previewUrls = [];
    }
    this.data.neoToken.token.previewUrls.push(newData);
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

  removeAssetUrlAtIndex(index: number) {
    this.data.neoToken.token.assetUrls.splice(index, 1);
  }

  getNewTokenData(): CreateNeoTokenRequest {
    this.data.neoToken.token.ownership.capitalization =
      this.capitalizationShares;

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
        ownership: JSON.parse(
          JSON.stringify(this.data.neoToken.token.ownership)
        ),
        transferOptions: this.transferOptions,
        revocable: this.revocable,
        expiry: 0, // TODO link this to form <<<<<<<<<<<<<<<<<<<<<<<<<
        renewable: this.renewable,
        metadata: JSON.parse(JSON.stringify(this.data.neoToken.token.metadata)),
      },
      listed: this.listed,
      contractId: this.currentSmartContract.id,
    };

    return newTokenData;
  }

  getUpdateTokenData(): UpdateNeoTokenRequest {
    this.data.neoToken.token.ownership.capitalization =
      this.capitalizationShares;

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
        ownership: JSON.parse(
          JSON.stringify(this.data.neoToken.token.ownership)
        ),
        transferOptions: this.transferOptions,
        revocable: this.revocable,
        expiry: 0, // TODO link this to form
        renewable: this.renewable,
        metadata: JSON.parse(JSON.stringify(this.data.neoToken.token.metadata)),
      },
      listed: this.listed,
      contractId: this.currentSmartContract.id,
    };

    return updateWalletData;
  }

  showSelectSmartContractDialog() {
    this.dialog.open(NeoSmartContractSelectDialogComponent, {
      width: "700px",
      data: {
        next: (result: NeoSmartContract) => {
          this.currentSmartContract = JSON.parse(JSON.stringify(result));
          this.tokenForm
            .get("contractId")
            .setValue(this.currentSmartContract.displayName);
        },
      },
    });
  }

  getCapitalizationSharesValidator(
    stakeholders: Array<StakeHolder>,
    capitalization: number
  ): ValidatorFn {
    return (control: AbstractControl): { [key: string]: boolean } | null => {
      let initialValue = 0;
      let sumOfStakeholderShares = stakeholders.reduce(
        (previousValue, currentValue) =>
          (previousValue += +currentValue.shares),
        initialValue
      );

      if (sumOfStakeholderShares > capitalization) {
        return { capitalizationError: true };
      }

      return null;
    };
  }

  showDialog(next) {
    this.dialog.open(WalletSelectDialogComponent, {
      width: "850px",
      data: {
        next: next,
        refresher: this,
      },
    });
  }

  showSelectWalletDialog() {
    this.showDialog((wallet: NeoWallet) => {
      this.currentWallet = JSON.parse(JSON.stringify(wallet));
      this.tokenForm
        .get("owner")
        .setValue(this.currentWallet.wallet?.accounts[0]?.address);
    });
  }

  close(saveChanges?: boolean) {
    if (!saveChanges) {
      this.data.neoToken.token.metadata = this.originalMetadata;
      this.data.neoToken.token.ownership = this.originalOwnership;
      this.dialogRef.close();
      return;
    }

    this.editorCard.validateMetadata(true);

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
