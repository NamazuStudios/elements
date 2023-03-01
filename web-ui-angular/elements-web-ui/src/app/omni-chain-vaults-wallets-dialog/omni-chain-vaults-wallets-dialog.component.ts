import { Component, Inject, OnInit, ViewChild } from '@angular/core';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatPaginator } from '@angular/material/paginator';
import { filter, tap } from 'rxjs/operators';
import { Vault } from '../api/models/omni/vaults';
import { Wallet } from '../api/models/omni/wallets';
import { WalletsService } from '../api/services/blockchain/omni/wallets.service';
import { ConfirmationDialogService } from '../confirmation-dialog/confirmation-dialog.service';
import { OmniChainWalletsDialogComponent } from '../omni-chain-wallets-dialog/omni-chain-wallets-dialog.component';
import { WalletsDataSource } from '../wallets.datasource';

interface Data {
  vault: Vault;
}

@Component({
  selector: 'app-omni-chain-vaults-wallets-dialog',
  templateUrl: './omni-chain-vaults-wallets-dialog.component.html',
  styleUrls: ['./omni-chain-vaults-wallets-dialog.component.css']
})
export class OmniChainVaultsWalletsDialogComponent implements OnInit {

  @ViewChild(MatPaginator) paginator: MatPaginator;
  displayedColumns: string[] = [
    'id',
    'name',
    'api',
    'editAction',
    'removeAction'
  ];
  dataSource: WalletsDataSource;
  vault: Vault;

  constructor(
    public dialogRef: MatDialogRef<OmniChainVaultsWalletsDialogComponent>,
    public dialog: MatDialog,
    private dialogService: ConfirmationDialogService,
    private walletsService: WalletsService,
    @Inject(MAT_DIALOG_DATA) public data: Data,
  ) {}

  ngOnInit(): void {
    this.vault = this.data.vault;
    this.dataSource = new WalletsDataSource(this.walletsService);
    this.refresh();
  }

  ngAfterViewInit(): void {
    this.paginator.page
      .pipe(
        tap(() => this.refresh())
      )
      .subscribe();
    this.dataSource.totalCount$.subscribe(totalCount => this.paginator.length = totalCount);
  }

  openEditWalletDialog(wallet: Wallet) {
    this.dialog.open(OmniChainWalletsDialogComponent, {
      panelClass: 'modal-container',
      data: {
        wallet,
        vault: this.vault,
        refresher: this,
      }
    });
  }

  openNewWalletDialog() {
    this.dialog.open(OmniChainWalletsDialogComponent, {
      panelClass: 'modal-container',
      data: {
        vault: this.vault,
        refresher: this,
      }
    });
  }

  deleteWallet(wallet: Wallet) {
    this.dialogService
      .confirm(
        "Confirm Dialog",
        `Are you sure you want to delete ${wallet.displayName}? Deletion is permanent.`
      )
      .pipe(filter((r) => r))
      .subscribe(() => {
        this.walletsService.deleteWallet(this.vault.id, wallet.id).subscribe(() => {
          this.refresh();
        });
      });
  }

  refresh(delay = 0) {
    setTimeout(() => {
      this.dataSource.loadWallets(
        this.vault.id,
        this.paginator.pageIndex * this.paginator.pageSize,
        this.paginator.pageSize,
      );
    }, delay);
  }

  close() {
    this.dialogRef.close();
  }
}
