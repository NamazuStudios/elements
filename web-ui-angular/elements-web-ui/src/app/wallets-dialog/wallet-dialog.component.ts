import { AfterViewInit, Component, Inject, OnInit } from "@angular/core";
import {
  MAT_DIALOG_DATA,
  MatDialogRef,
  MatDialog,
} from "@angular/material/dialog";
import { MatSnackBar } from "@angular/material/snack-bar";
import { FormBuilder, Validators } from "@angular/forms";
import { AlertService } from "../alert.service";
import { NeoWalletsService, UsersService } from "../api/services";
import { User } from "../api/models";
import { CreateNeoWalletRequest } from "../api/models/blockchain/create-neo-wallet-request";
import { UserSelectDialogComponent } from "../user-select-dialog/user-select-dialog.component";
import { UpdateNeoWalletRequest } from "../api/models/blockchain/update-neo-wallet-request";
import { Clipboard } from "@angular/cdk/clipboard";

export interface TimeStrategyType {
  key: string;
  description: string;
}

export interface ScoreStrategyType {
  key: string;
  description: string;
}

@Component({
  selector: "app-wallet-dialog",
  templateUrl: "./wallet-dialog.component.html",
  styleUrls: ["./wallet-dialog.component.css"],
})
export class WalletDialogComponent implements OnInit, AfterViewInit {
  private confirmNewPassword: string = "";
  privateKeyDisplayed: boolean = false;

  // TODO: make sure "wallet exists" validator implemented

  walletForm = this.formBuilder.group({
    displayName: [this.data.wallet.displayName, [Validators.required]],
    walletUserName: [{ value: this.data.wallet.user?.name, disabled: true }],
    walletPassword: [""],
    newWalletPassword: [""],
    walletConfirmPassword: [""],
  });

  get displayName(): string {
    return this.walletForm.get("displayName").value;
  }

  get walletPassword(): string {
    return this.walletForm.get("walletPassword").value;
  }

  get newWalletPassword(): string {
    return this.walletForm.get("newWalletPassword").value;
  }

  constructor(
    public dialogRef: MatDialogRef<WalletDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private formBuilder: FormBuilder,
    private alertService: AlertService,
    private neoWalletsService: NeoWalletsService,
    private usersService: UsersService,
    public dialog: MatDialog,
    private clipboard: Clipboard,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit() {
    this.data.wallet.userId = this.data.wallet.user?.id; // save the current owner's id

    this.alertService.getMessage().subscribe((message: any) => {
      if (message) {
        this.snackBar.open(message.text, "Dismiss", { duration: 3000 });
      }
    });
  }

  ngAfterViewInit() {
    if (this.data.isNew) {
      this.usersService.getUser().subscribe((user: User) => {
        this.data.wallet.user = JSON.parse(JSON.stringify(user));
        this.walletForm.controls["walletUserName"].setValue(
          this.data.wallet.user?.name
        );
      });
    }
  }

  showSelectUserDialog() {
    this.dialog.open(UserSelectDialogComponent, {
      width: "700px",
      data: {
        next: (result: User) => {
          this.data.wallet.user = result;
          this.walletForm.controls["walletUserName"].setValue(result.name);
          this.walletForm.updateValueAndValidity();
        },
      },
    });
  }

  getNewWalletData(): CreateNeoWalletRequest {
    let newWalletData: CreateNeoWalletRequest = {
      displayName: "",
      userId: "",
      password: "",
    };

    newWalletData.displayName = this.displayName;

    if (this.data.wallet.user.id !== undefined) {
      newWalletData.userId = this.data.wallet.user.id;
    }

    if (this.walletPassword) {
      newWalletData.password = this.walletPassword;
    } else {
      delete newWalletData.password;
    }

    return newWalletData;
  }

  getUpdateWalletData(): UpdateNeoWalletRequest {
    let updateWalletData: UpdateNeoWalletRequest = {
      displayName: "",
      userId: "",
      newUserId: "",
      password: "",
      newPassword: "",
      walletId: "",
    };

    updateWalletData.displayName = this.displayName;
    updateWalletData.userId = this.data.wallet.userId;
    updateWalletData.newUserId = this.data.wallet.user.id;
    updateWalletData.password = this.walletPassword;
    updateWalletData.newPassword = this.newWalletPassword;
    updateWalletData.walletId = this.data.wallet.id;

    return updateWalletData;
  }

  copyKeyToClipboard(data: string) {
    this.clipboard.copy(data);
    this.snackBar.open("Key has been copied to clipboard.", "Dismiss", {
      duration: 3000,
    });
  }

  showPrivateKey() {
    this.privateKeyDisplayed = true;
  }

  close(saveChanges?: boolean) {
    if (!saveChanges) {
      this.dialogRef.close();
      return;
    }

    let walletData;
    this.data.isNew
      ? (walletData = this.getNewWalletData())
      : (walletData = this.getUpdateWalletData());

    this.data.next(walletData).subscribe(
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
