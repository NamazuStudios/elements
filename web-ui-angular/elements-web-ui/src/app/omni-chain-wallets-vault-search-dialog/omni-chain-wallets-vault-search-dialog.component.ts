import { Component, Inject, OnInit, ViewChild } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatPaginator } from '@angular/material/paginator';
import { tap } from 'rxjs/operators';
import { Vault } from '../api/models/omni/vaults';
import { VaultsService } from '../api/services/blockchain/omni/vaults.service';
import { AuthenticationService } from '../authentication.service';
import { VaultsDataSource } from '../vaults.datasource';

interface Data {
  onSelect: (vault: Vault) => void;
}

@Component({
  selector: 'app-omni-chain-wallets-vault-search-dialog',
  templateUrl: './omni-chain-wallets-vault-search-dialog.component.html',
  styleUrls: ['./omni-chain-wallets-vault-search-dialog.component.css']
})
export class OmniChainWalletsVaultSearchDialogComponent implements OnInit {

  @ViewChild(MatPaginator) paginator: MatPaginator;
  displayedColumns: string[] = [
    'id',
    'user',
    'name',
    'selectAction',
  ];
  dataSource: VaultsDataSource;
  vaults: Vault[];

  constructor(
    public dialogRef: MatDialogRef<OmniChainWalletsVaultSearchDialogComponent>,
    private vaultsService: VaultsService,
    private authService: AuthenticationService,
    @Inject(MAT_DIALOG_DATA) public data: Data,
  ) { }

  ngOnInit(): void {
    this.dataSource = new VaultsDataSource(this.vaultsService);
    this.refresh();
  }

  ngAfterViewInit() {
    this.paginator.page
      .pipe(
        tap(() => this.refresh())
      )
      .subscribe();

    this.dataSource.vaults$.subscribe(vaults => this.vaults = vaults);
    this.dataSource.totalCount$.subscribe(totalCount => this.paginator.length = totalCount);
  }

  refresh(delay = 0) {
    setTimeout(() => {
      this.dataSource.loadVaults(
        this.paginator.pageIndex * this.paginator.pageSize,
        this.paginator.pageSize,
        ''
      );
    }, delay);
  }

  onSelect(vault: Vault) {
    this.data.onSelect(vault);
    this.close();
  }

  close() {
    this.dialogRef.close();
  }

}
