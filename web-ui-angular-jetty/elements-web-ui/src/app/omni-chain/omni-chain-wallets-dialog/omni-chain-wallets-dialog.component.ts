import { Component, Inject, OnInit } from '@angular/core';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { Vault } from '../../api/models/omni/vaults';
import { Wallet, WalletAccount } from '../../api/models/omni/wallets';
import { WalletsService } from '../../api/services/blockchain/omni/wallets.service';
import { OmniChainWalletsAccountsDialogComponent } from '../omni-chain-wallets-accounts-dialog/omni-chain-wallets-accounts-dialog.component';
import { OmniChainWalletsVaultSearchDialogComponent } from '../omni-chain-wallets-vault-search-dialog/omni-chain-wallets-vault-search-dialog.component';

interface Data {
  wallet: Wallet;
  vault: Vault;
  refresher: {
    refresh: (value?: number) => void;
  }
}

const API_NETWORK_MAP = {
  ETHEREUM: ['ETHEREUM', 'ETHEREUM_TEST', 'BSC', 'BSC_TEST', 'POLYGON', 'POLYGON_TEST'],
  FLOW: ['FLOW', 'FLOW_TEST'],
  NEO: ['NEO', 'NEO_TEST'],
  SOLANA: ['SOLANA', 'SOLANA_TEST'],
  NEAR: ['NEAR', 'NEAR_TEST']
}

@Component({
  selector: 'app-omni-chain-wallets-dialog',
  templateUrl: './omni-chain-wallets-dialog.component.html',
  styleUrls: ['./omni-chain-wallets-dialog.component.css']
})
export class OmniChainWalletsDialogComponent implements OnInit {

  apis = [
    { value: 'ETHEREUM', label: 'ETHEREUM' },
    { value: 'FLOW', label: 'FLOW' },
    { value: 'NEO', label: 'NEO' },
    { value: 'SOLANA', label: 'SOLANA' },
    { value: 'NEAR', label: 'NEAR' }
  ]
  displayName = '';
  api = '';
  vaultId = '';
  vaultName = '';
  networks: string[] = [];
  accounts: WalletAccount[] = [];
  preferredAccount: number = 0;
  wallet: Wallet;
  error = '';

  constructor(
    public dialogRef: MatDialogRef<OmniChainWalletsDialogComponent>,
    public dialog: MatDialog,
    private walletsService: WalletsService,
    @Inject(MAT_DIALOG_DATA) public data: Data,
  ) { }

  ngOnInit(): void {
    if (this.data.wallet) {
      this.wallet = this.data.wallet;
      this.displayName = this.wallet.displayName;
      this.api = this.wallet.api;
      this.networks = this.wallet.networks;
      this.accounts = this.wallet.accounts;
      this.preferredAccount = this.wallet.preferredAccount;
    }
    if (this.data.vault) {
      this.vaultId = this.data.vault.id;
      this.vaultName = this.data.vault.displayName;
    }
  }

  get isSubmitDisabled(): boolean {
    if (!this.displayName || !this.api || !this.vaultId || this.networks.length === 0) {
      return true;
    }
    return false;
  }

  openVaultsSearchDialog() {
    this.dialog.open(OmniChainWalletsVaultSearchDialogComponent, {
      panelClass: 'modal-container',
      data: {
        onSelect: (vault: Vault) => {
          this.vaultId = vault.id;
          this.vaultName = vault.displayName;
        }
      }
    });
  }

  openWalletAccountsDialog() {
    this.dialog.open(OmniChainWalletsAccountsDialogComponent, {
      panelClass: 'modal-container',
      data: {
        preferredAccount: this.preferredAccount,
        accounts: this.accounts,
        wallet: this.wallet,
        onComplete: (accounts: WalletAccount[], preferredAccount: number) => {
          this.accounts = accounts;
          this.preferredAccount = preferredAccount;
          this.error = '';
        }
      }
    });
  }

  changeApi(value: string) {
    this.api = value;
    this.networks = [];
  }

  isNetworkSelected(network: string): boolean {
    return !!this.networks.find((net) => net === network);
  }

  isNetworkDisabled(network: string): boolean {
    const availableNetworks = API_NETWORK_MAP[this.api];
    if (availableNetworks && !this.wallet) {
      return !API_NETWORK_MAP[this.api].includes(network);
    }
    return true;
  }

  toggleNetwork(network: string) {
    const selected = this.isNetworkSelected(network);
    if (selected) {
      this.networks = this.networks.filter((net) => net !== network);
    } else {
      this.networks.push(network);
    }
  }

  submit() {
    if (this.accounts.length === 0 || !this.preferredAccount) {
      this.error = 'Please add accounts';
      return;
    }
    if (this.wallet) {
      this.walletsService.editWallet({
        vaultId: this.vaultId,
        walletId: this.wallet.id,
        body: {
          displayName: this.displayName,
          preferredAccount: this.preferredAccount,
          networks: this.networks,
        },
      })
      .subscribe(() => {
        this.data.refresher.refresh();
        this.close();
      });
    } else {
      this.walletsService.createWallet({
        vaultId: this.vaultId,
        body: {
          api: this.api,
          networks: this.networks,
          displayName: this.displayName,
          preferredAccount: this.preferredAccount,
          accounts: this.accounts,
        },
      })
      .subscribe(() => {
        this.data.refresher.refresh();
        this.close();
      });
    }
  }

  close() {
    this.dialogRef.close();
  }

}
