import { Component, Inject, Input, OnInit } from '@angular/core';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { Contract } from '../api/models/omni/contracts';
import { Vault } from '../api/models/omni/vaults';
import { ContractsService } from '../api/services/blockchain/omni/contracts.service';
import { OmniChainVaultsDialogComponent } from '../omni-chain-vaults-dialog/omni-chain-vaults-dialog.component';
import { OmniChainWalletsVaultSearchDialogComponent } from '../omni-chain-wallets-vault-search-dialog/omni-chain-wallets-vault-search-dialog.component';

const NETWORKS = [
  'ETHEREUM', 'ETHEREUM_TEST', 'BSC', 'BSC_TEST', 'POLYGON', 'POLYGON_TEST',
  'FLOW', 'FLOW_TEST',
  'NEO', 'NEO_TEST',
  'SOLANA', 'SOLANA_TEST',
  'NEAR', 'NEAR_TEST'
];

interface NetworkAddresses {
  address: string;
  network: string;
}

interface Metadata {
  key: string;
  value: string;
}

interface Data {
  contract: Contract,
  refresher: {
    refresh: (delay?: number) => void;
  }
}

@Component({
  selector: 'app-omni-chain-contracts-dialog',
  templateUrl: './omni-chain-contracts-dialog.component.html',
  styleUrls: ['./omni-chain-contracts-dialog.component.css']
})
export class OmniChainContractsDialogComponent implements OnInit {
  name = '';
  displayName = '';
  vaultId = '';
  vaultName = '';
  addresses: NetworkAddresses[] = [];
  networks: string[] = [];
  availableNetworks: string [] = [];
  metadata: Metadata[] = [];
  newNetwork: string = '';
  newAddress: string = '';
  newKey: string = '';
  newValue: string = '';
  contract: Contract;

  constructor(
    public dialog: MatDialog,
    public dialogRef: MatDialogRef<OmniChainVaultsDialogComponent>,
    private contractService: ContractsService,
    @Inject(MAT_DIALOG_DATA) public data: Data,
  ) { }

  ngOnInit(): void {
    this.networks = NETWORKS;
    this.availableNetworks = NETWORKS;
    if (this.data.contract) {
      this.contract = this.data.contract;
      this.name = this.contract.name;
      this.displayName = this.contract.displayName;
      this.vaultId = this.contract.vault.id;
      this.vaultName = this.contract.vault.displayName;
      const addresses = [];
      const metadata = [];
      for (const key in this.contract.addresses) {
        const address = this.contract.addresses[key].address;
        const network = key;
        addresses.push({
          address,
          network,
        });
      }
      for (const key in this.contract.metadata) {
        const value = this.contract.metadata[key];
        metadata.push({
          key,
          value,
        });
      }
      this.addresses = addresses;
      this.metadata = metadata;
    }
  }

  get isSubmitDisabled(): boolean {
    if (!this.name || !this.displayName || !this.vaultId) {
      return true;
    }
    return false;
  }

  changeNewNetwork(value: string): void {
    this.newNetwork = value;
  }

  changeNewAddress(value: string): void {
    this.newAddress = value;
  }

  changeNewKey(value: string): void {
    this.newKey = value;
  }

  changeNewValue(value: string): void {
    this.newValue = value;
  }

  addNewAddress(): void {
    this.addresses.push({
      network: this.newNetwork,
      address: this.newAddress,
    });
    this.availableNetworks = this.networks.filter((network) => network !== this.newNetwork);
    this.newNetwork = '';
    this.newAddress = '';
  }

  removeAddress(address: NetworkAddresses): void {
    this.addresses = this.addresses.filter(
      (add) => add.network !== address.network,
    );
    this.networks.push(address.network);
  }

  openVaultsSearchDialog(): void {
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

  addNewMetadata(): void {
    this.metadata.push({
      key: this.newKey,
      value: this.newValue,
    });
    this.newKey = '';
    this.newValue = '';
  }

  removeMetadata(key: string): void {
    this.metadata = this.metadata.filter(
      (item) => item.key !== key,
    );
  }

  isKeyUsed(): boolean {
    return !!this.metadata.find((item) => item.key === this.newKey);
  }

  convertAddressess() {
    const addresses = {};
    for (let i = 0; i < this.addresses.length; i++) {
      const address = this.addresses[i];
      addresses[address.network] = {
        address: address.address,
      }
    }
    return addresses;
  }

  convertMetadata() {
    const metadata = {};
    for (let i = 0; i < this.metadata.length; i++) {
      const item = this.metadata[i];
      metadata[item.key] = item.value;
    }
    return metadata;
  }

  editContract() {
    this.contractService.editContract({
      contractId: this.contract.id,
      body: {
        displayName: this.displayName,
        name: this.name,
        vaultId: this.vaultId,
        addresses: this.convertAddressess(),
        metadata: this.convertMetadata(),
      },
    })
    .subscribe(() => {
      this.data.refresher.refresh();
      this.close();
    });
  }

  createContract() {
    this.contractService.createContract({
      displayName: this.displayName,
      name: this.name,
      vaultId: this.vaultId,
      addresses: this.convertAddressess(),
      metadata: this.convertMetadata(),
    })
    .subscribe(() => {
      this.data.refresher.refresh();
      this.close();
    });
  }

  submit() {
    if (this.contract) {
      this.editContract();
    } else {
      this.createContract();
    }
  }

  close() {
    this.dialogRef.close();
  }
}
