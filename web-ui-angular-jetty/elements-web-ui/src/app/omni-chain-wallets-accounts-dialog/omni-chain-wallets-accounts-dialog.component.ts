import { Component, Inject, OnInit } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { Wallet, WalletAccount } from '../api/models/omni/wallets';

interface Data {
  accounts: WalletAccount[];
  preferredAccount: number;
  wallet: Wallet;
  onComplete: (accounts: WalletAccount[], preferredAccount: number) => void;
}

@Component({
  selector: 'app-omni-chain-wallets-accounts-dialog',
  templateUrl: './omni-chain-wallets-accounts-dialog.component.html',
  styleUrls: ['./omni-chain-wallets-accounts-dialog.component.css']
})
export class OmniChainWalletsAccountsDialogComponent implements OnInit {

  accounts: WalletAccount[] = [];
  preferredAccount: number = null;
  accountIndexes: number[] = [];
  newGenerate = false;
  newAddress = '';
  newPrivateKey = '';
  wallet: Wallet;

  constructor(
    public dialogRef: MatDialogRef<OmniChainWalletsAccountsDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: Data,
  ) { }

  ngOnInit(): void {
    this.preferredAccount = this.data.preferredAccount;
    this.accounts = this.data.accounts || [];
    this.updateAccountIndexes();
    if (this.data.wallet) {
      this.wallet = this.data.wallet;
    }
  }

  get isSubmitDisabled(): boolean {
    if (this.accounts.length === 0) {
      return true;
    }
    if (!this.preferredAccount) {
      return true;
    }
    return false;
  }

  get isPrivateKeyHidden(): boolean {
    return true;
  }

  changeNewGenerate(value: boolean) {
    this.newGenerate = value;
    if (value) {
      this.newAddress = '';
      this.newPrivateKey = '';
    }
  }

  addNewAccount() {
    this.accounts.push({
      generate: this.newGenerate,
      address: this.newAddress,
      privateKey: this.newPrivateKey,
    });
    this.newGenerate = false;
    this.newAddress = '';
    this.newPrivateKey = '';
    this.updateAccountIndexes();
    if (!this.preferredAccount) {
      this.preferredAccount = 1;
    }
  }

  removeAccount(accountIndex: number) {
    this.accounts = this.accounts.filter((_acc, index) => index !== accountIndex);
    this.updateAccountIndexes();
  }

  updateAccountIndexes() {
    const newIndexes = [];
    for (let i = 0; i < this.accounts.length; i++) {
      newIndexes.push(i + 1);
    }
    this.accountIndexes = newIndexes;
  }

  submit() {
    this.data.onComplete(
      this.accounts.map((account) => ({
        ...account,
        address: account.generate ? null : account.address,
        privateKey: account.generate ? null : account.privateKey,
      })),
      this.preferredAccount,
    );
    this.close();
  }

  close() {
    this.dialogRef.close();
  }
}
