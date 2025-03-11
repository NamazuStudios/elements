import { SelectionModel } from '@angular/cdk/collections';
import { Component, OnInit, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatPaginator } from '@angular/material/paginator';
import { filter, tap } from 'rxjs/operators';
import { Vault } from '../../api/models/omni/vaults';
import { VaultsService } from '../../api/services/blockchain/omni/vaults.service';
import { AuthenticationService } from '../../login/authentication.service';
import { ConfirmationDialogService } from '../../confirmation-dialog/confirmation-dialog.service';
import { OmniChainVaultsDialogComponent } from '../omni-chain-vaults-dialog/omni-chain-vaults-dialog.component';
import { OmniChainVaultsWalletsDialogComponent } from '../omni-chain-vaults-wallets-dialog/omni-chain-vaults-wallets-dialog.component';
import { VaultsDataSource } from '../vaults.datasource';

@Component({
  selector: 'app-omni-chain-vaults',
  templateUrl: './omni-chain-vaults.component.html',
  styleUrls: ['./omni-chain-vaults.component.css']
})
export class OmniChainVaultsComponent implements OnInit {

  @ViewChild(MatPaginator) paginator: MatPaginator;
  displayedColumns: string[] = [
    'select',
    'id',
    'user',
    'displayName',
    'encrypted',
    'walletsAction',
    'contractsAction',
    'editAction',
    'removeAction',
  ];
  dataSource: VaultsDataSource;
  vaults: Vault[];
  hasSelection = false;
  selection: SelectionModel<Vault>;
  search = '';
  userType: string | null = null;

  constructor(
    public dialog: MatDialog,
    private vaultsService: VaultsService,
    private dialogService: ConfirmationDialogService,
    private authService: AuthenticationService,
  ) { }

  ngOnInit(): void {
    this.selection = new SelectionModel<Vault>(true, []);
    this.dataSource = new VaultsDataSource(this.vaultsService, this.authService);
    this.refresh();
  }

  ngAfterViewInit(): void {
    this.paginator.page
      .pipe(
        tap(() => this.refresh())
      )
      .subscribe();
    this.selection.changed.subscribe(s => this.hasSelection = this.selection.hasValue());
    this.dataSource.vaults$.subscribe(vaults => this.vaults = vaults);
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
      this.dataSource.loadVaults(
        this.paginator.pageIndex * this.paginator.pageSize,
        this.paginator.pageSize,
        this.search,
        this.userType,
      );
    }, delay);
  }

  isAllSelected() {
    const numSelected = this.selection.selected.length;
    const numRows = this.vaults.length;
    return numSelected == numRows;
  }

  masterToggle() {
    this.isAllSelected() ?
      this.selection.clear() :
      this.vaults.forEach(row => this.selection.select(row));
  }

  deleteVault(vault: Vault) {
    this.dialogService
      .confirm(
        "Confirm Dialog",
        `Are you sure you want to delete ${vault.displayName}? Deletion is permanent.`
      )
      .pipe(filter((r) => r))
      .subscribe(() => {
        this.vaultsService.deleteVault(vault.id).subscribe(() => {
          this.refresh();
        });
      });
  }

  deleteSelectedVaults() {
    this.dialogService
      .confirm('Confirm Dialog', `Are you sure you want to delete the ${this.selection.selected.length} selected vault${this.selection.selected.length == 1 ? '' : 's'}? Deletion is permanent.`)
      .pipe(filter(r => r))
      .subscribe(() => {
        this.selection.selected.forEach(vault => {
          this.vaultsService.deleteVault(vault.id).subscribe(() => {
            this.refresh();
          });
        });
        this.selection.clear();
        this.refresh();
      });
  }

  openNewVaultDialog() {
    this.dialog.open(OmniChainVaultsDialogComponent, {
      panelClass: 'modal-container',
      data: {
        vault: null,
        refresher: this,
      }
    });
  }

  openEditVaultDialog(vault: Vault) {
    this.dialog.open(OmniChainVaultsDialogComponent, {
      panelClass: 'modal-container',
      data: {
        vault,
        refresher: this,
      }
    });
  }

  openWalletsDialog(vault: Vault) {
    this.dialog.open(OmniChainVaultsWalletsDialogComponent, {
      panelClass: 'modal-container',
      data: {
        vault,
      }
    });
  }
}
