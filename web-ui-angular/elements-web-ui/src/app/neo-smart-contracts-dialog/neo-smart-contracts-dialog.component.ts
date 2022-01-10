import { Component, Inject, OnInit } from "@angular/core";
import {
  MAT_DIALOG_DATA,
  MatDialogRef,
  MatDialog,
} from "@angular/material/dialog";
import { MatSnackBar } from "@angular/material/snack-bar";
import { FormBuilder } from "@angular/forms";
import { AlertService } from "../alert.service";
import { COMMA, ENTER } from "@angular/cdk/keycodes";
import { UserLevel } from "../user-dialog/user-dialog.component";
import { NeoSmartContract } from "../api/models/blockchain/neo-smart-contract";
import { PatchNeoSmartContractRequest } from "../api/models/blockchain/patch-neo-smart-contract-request";

@Component({
  selector: 'app-neo-smart-contracts-dialog',
  templateUrl: './neo-smart-contracts-dialog.component.html',
  styleUrls: ['./neo-smart-contracts-dialog.component.css']
})
export class NeoSmartContractsDialogComponent implements OnInit {

  selectable = true;
  removable = true;
  addOnBlur = true;
  readonly separatorKeyCodes: number[] = [ENTER, COMMA];
  public blockchainDisabled: boolean = true;
  selectedBlockchain = "NEO";

  blockchainOptions: UserLevel[] = [
    { key: "NEO", description: "NEO Blockchain" },
    { key: "NONE", description: "None selected" },
  ];

  neoSmartContractForm = this.formBuilder.group({
    displayName: [this.data.neoSmartContract.displayName],
    scriptHash: [this.data.neoSmartContract.scriptHash],

    blockchain: [
      {
        value: this.data.neoSmartContract.blockchain,
        disabled: this.blockchainDisabled,
      },
    ],
    // TODO: Metadata
  });

  get displayName(): string {
    return this.neoSmartContractForm.get("displayName").value;
  }

  get scriptHash(): string {
    return this.neoSmartContractForm.get("scriptHash").value;
  }

  get blockchain(): NeoSmartContract["blockchain"] {
    return this.neoSmartContractForm.get("blockchain").value;
  }

  constructor(
    public dialogRef: MatDialogRef<NeoSmartContractsDialogComponent>,
    @Inject(MAT_DIALOG_DATA)
    public data: {
      isNew: boolean;
      neoSmartContract: NeoSmartContract;
      next: any;
      refresher: any;
    },
    // @Inject(MAT_DIALOG_DATA) public data: any,
    private formBuilder: FormBuilder,
    private alertService: AlertService,
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

  getNewNeoSmartContractData(): PatchNeoSmartContractRequest {
    let newNeoSmartContractData: PatchNeoSmartContractRequest = {
      displayName: this.displayName,
      scriptHash: this.scriptHash,
      blockchain: "NEO",
      // todo: metadata
    };

    return newNeoSmartContractData;
  }

  // NOTE: were using Patch for both new and updated Smart Contract requests...
  getUpdateNeoSmartContractData(): PatchNeoSmartContractRequest {
    let updateNeoSmartContractData: PatchNeoSmartContractRequest = {
      displayName: this.displayName,
      scriptHash: this.scriptHash,
      blockchain: this.blockchain,
      // todo: metadata
    };

    return updateNeoSmartContractData;
  }

  close(saveChanges?: boolean) {
    if (!saveChanges) {
      this.dialogRef.close();
      return;
    }

    let neoSmartContractData;
    this.data.isNew
      ? (neoSmartContractData = this.getNewNeoSmartContractData())
      : (neoSmartContractData = this.getUpdateNeoSmartContractData());

    this.data.next(neoSmartContractData).subscribe(
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
