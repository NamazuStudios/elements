import { SelectionModel } from '@angular/cdk/collections';
import { Component, OnInit, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatPaginator } from '@angular/material/paginator';
import { filter, tap } from 'rxjs/operators';
import { Wallet } from '../../api/models/omni/wallets';
import { WalletsService } from '../../api/services/blockchain/omni/wallets.service';
import { ConfirmationDialogService } from '../../confirmation-dialog/confirmation-dialog.service';
import { OmniChainWalletsDialogComponent } from '../omni-chain-wallets-dialog/omni-chain-wallets-dialog.component';
import { WalletsDataSource } from '../wallets.datasource';

@Component({
  selector: 'app-omni-chain-wallets',
  templateUrl: './omni-chain-wallets.component.html',
  styleUrls: ['./omni-chain-wallets.component.css']
})
export class OmniChainWalletsComponent implements OnInit {

  @ViewChild(MatPaginator) paginator: MatPaginator;
  displayedColumns: string[] = [
    'select',
    'id',
    'displayName',
    'api',
    'vaultName',
    'vaultId',
    'editAction',
    'removeAction',
  ];
  wallets: Wallet[] = [];
  dataSource: WalletsDataSource;
  hasSelection = false;
  selection: SelectionModel<Wallet>;
  search = '';
  userType: string | null = null;

  constructor(
    public dialog: MatDialog,
    private walletsService: WalletsService,
    private dialogService: ConfirmationDialogService,
  ) { }

  ngOnInit(): void {
    this.selection = new SelectionModel<Wallet>(true, []);
    this.dataSource = new WalletsDataSource(this.walletsService);
    this.refresh();
  }

  ngAfterViewInit(): void {
    this.paginator.page
      .pipe(
        tap(() => this.refresh())
      )
      .subscribe();
    this.selection.changed.subscribe(s => this.hasSelection = this.selection.hasValue());
    this.dataSource.wallets$.subscribe(wallets => this.wallets = wallets);
    this.dataSource.totalCount$.subscribe(totalCount => this.paginator.length = totalCount);
  }

  changeSearch(value: string) {
    this.search = value;
    this.refresh();
  }

  changeUserType(value: string) {
    this.userType = value;
    this.refresh();
  }

  refresh(delay = 0) {
    setTimeout(() => {
      this.selection.clear();
      this.dataSource.loadWallets(
        null,
        this.paginator.pageIndex * this.paginator.pageSize,
        this.paginator.pageSize,
        this.search || null,
        this.userType,
      );
    }, delay);
  }

  isAllSelected() {
    const numSelected = this.selection.selected.length;
    const numRows = this.wallets.length;
    return numSelected == numRows;
  }

  masterToggle() {
    this.isAllSelected() ?
      this.selection.clear() :
      this.wallets.forEach(row => this.selection.select(row));
  }

  deleteWallet(wallet: Wallet) {
    this.dialogService
      .confirm(
        'Confirm Dialog',
        `Are you sure you want to delete ${wallet.displayName}? Deletion is permanent.`
      )
      .pipe(filter((r) => r))
      .subscribe(() => {
        this.walletsService.deleteWallet(wallet.vault.id, wallet.id).subscribe(() => {
          this.refresh();
        });
      });
  }

  deleteSelectedWallets() {
    this.dialogService
      .confirm('Confirm Dialog', `Are you sure you want to delete the ${this.selection.selected.length} selected wallet${this.selection.selected.length == 1 ? '' : 's'}? Deletion is permanent.`)
      .pipe(filter(r => r))
      .subscribe(() => {
        this.selection.selected.forEach(wallet => {
          this.walletsService.deleteWallet(wallet.vault.id, wallet.id).subscribe(() => {
            this.refresh();
          });
        });
        this.selection.clear();
        this.refresh();
      });
  }

  openNewWalletDialog() {
    this.dialog.open(OmniChainWalletsDialogComponent, {
      panelClass: 'modal-container',
      data: {
        refresher: this,
      }
    });
  }

  openEditWalletDialog(wallet: Wallet) {
    this.dialog.open(OmniChainWalletsDialogComponent, {
      panelClass: 'modal-container',
      data: {
        wallet,
        vault: wallet.vault,
        refresher: this,
      }
    });
  }
}
