import { Component, Inject, OnInit } from '@angular/core';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { User } from '../api/models';
import { UpdateVaultRequest, Vault } from '../api/models/omni/vaults';
import { VaultsService } from '../api/services/blockchain/omni/vaults.service';
import { AuthenticationService } from '../authentication.service';
import { OmniChainUserSearchDialogComponent } from '../omni-chain-user-search-dialog/omni-chain-user-search-dialog.component';

interface Data {
  refresher: {
    refresh: (value?: number) => void;
  };
  vault: Vault;
}

@Component({
  selector: 'app-omni-chain-vaults-dialog',
  templateUrl: './omni-chain-vaults-dialog.component.html',
  styleUrls: ['./omni-chain-vaults-dialog.component.css']
})
export class OmniChainVaultsDialogComponent implements OnInit {
  displayName: string = '';
  algorithm: string = 'RSA_256';
  userId: string = '';
  userName: string = '';
  pass: string = '';
  passConfirm: string = '';
  algorithms = [
    { value: 'RSA_512', label: 'RSA 512' },
    { value: 'RSA_384', label: 'RSA 384' },
    { value: 'RSA_256', label: 'RSA 256' },
    { value: 'ECDSA_512', label: 'ECDSA 512' },
    { value: 'ECDSA_384', label: 'ECDSA 384' },
    { value: 'ECDSA_256', label: 'ECDSA 256' },
  ];
  vault: Vault;
  error = '';
  showPrivateKey: boolean;
  showEncryption: boolean;
  encrypted: boolean;

  constructor(
    public dialogRef: MatDialogRef<OmniChainVaultsDialogComponent>,
    public dialog: MatDialog,
    private vaultService: VaultsService,
    private authService: AuthenticationService,
    @Inject(MAT_DIALOG_DATA) public data: Data,
  ) { }

  get isSuperUser(): boolean {
    return this.authService.currentSession.session.user.level === 'SUPERUSER';
  }

  get submitDisabled(): boolean {
    if (this.vault) {
      return !this.displayName || !this.userId;
    }
    return !this.pass || !this.algorithm || !this.userId || !this.displayName;
  }

  ngOnInit(): void {
    this.vault = this.data?.vault;
    if (this.data?.vault) {
      this.vault = this.data?.vault;
      this.displayName = this.vault.displayName;
      this.algorithm = this.vault.key.algorithm;
      this.userId = this.vault.user.id;
      this.userName = this.vault.user.name;
      this.encrypted = this.vault.key.encrypted;
    }
  }

  openSearchUsersDialog() {
    this.dialog.open(OmniChainUserSearchDialogComponent, {
      panelClass: 'modal-container',
      data: {
        onSelect: (user: User) => {
          this.userId = user.id;
          this.userName = user.name;
        },
      }
    });
  }

  changeAlgorithm(value: string) {
    this.algorithm = value;
  }

  createVault() {
    this.vaultService.createVault({
      userId: this.userId,
      displayName: this.displayName,
      algorithm: this.algorithm || null,
      passphrase: this.pass,
    }).subscribe(() => {
      this.data.refresher.refresh();
      this.close();
    });
    this.error = '';
  }

  editVault() {
    let body: UpdateVaultRequest = {
      userId: this.userId,
      displayName: this.displayName,
    };
    if (this.pass) {
      body.newPassphrase = this.pass;
    }
    this.vaultService.editVault({
      id: this.vault.id,
      body,
    })
    .subscribe(() => {
      this.data.refresher.refresh();
      this.close();
    });
    this.error = '';
  }

  submit() {
    if (this.vault) {
      this.editVault();
    } else {
      if (this.pass === this.passConfirm) {
        this.createVault();
      } else {
        this.error = 'Passwords doesn\'t match';
      }
    }
  }

  togglePrivateKey() {
    this.showPrivateKey = true;
  }

  toggleEncryption() {
    this.showEncryption = true;
  }

  close() {
    this.dialogRef.close();
  }
}
