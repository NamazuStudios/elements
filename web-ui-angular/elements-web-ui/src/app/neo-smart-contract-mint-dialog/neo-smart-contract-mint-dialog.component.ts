import { Component, Inject, OnInit } from "@angular/core";
import {
  MAT_DIALOG_DATA,
  MatDialogRef,
  MatDialog,
} from "@angular/material/dialog";
import { MatSnackBar } from "@angular/material/snack-bar";
import { FormBuilder } from "@angular/forms";
import { AlertService } from "../alert.service";
import { WalletSelectDialogComponent } from "../wallet-select-dialog/wallet-select-dialog.component";

import { MintTokenRequest } from "../api/models/blockchain/mint-token-request";

import { NeoWallet } from "../api/models/blockchain/neo-wallet";

@Component({
  selector: "app-neo-smart-contract-mint-dialog",
  templateUrl: "./neo-smart-contract-mint-dialog.component.html",
  styleUrls: ["./neo-smart-contract-mint-dialog.component.css"],
})
export class NeoSmartContractMintDialogComponent implements OnInit {
  mintForm = this.formBuilder.group({
    walletId: [{value: "", disabled: true}],
    password: [""],
  });

  constructor(
    public dialogRef: MatDialogRef<NeoSmartContractMintDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    public dialog: MatDialog,
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
      this.mintForm.get("walletId").setValue(wallet.id);
    });
  }

  close(res?: any) {
    if (!res) {
      this.dialogRef.close();
      return;
    }

    let mintTokenRequest: MintTokenRequest = {
      tokenIds: [this.data.neoToken.id],
      walletId: this.mintForm.get("walletId").value,
      password: this.mintForm.get("password").value,
    };

    this.data.next(mintTokenRequest).subscribe(
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
