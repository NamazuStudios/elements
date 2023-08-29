import { SelectionModel } from '@angular/cdk/collections';
import { Component, OnInit, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatPaginator } from '@angular/material/paginator';
import { filter, tap } from 'rxjs/operators';
import { Contract, ContractAddresses } from '../api/models/omni/contracts';
import { ContractsService } from '../api/services/blockchain/omni/contracts.service';
import { ConfirmationDialogService } from '../confirmation-dialog/confirmation-dialog.service';
import { ContractsDataSource } from '../contracts.datasource';
import { OmniChainContractsDialogComponent } from '../omni-chain-contracts-dialog/omni-chain-contracts-dialog.component';

const API_NETWORK_MAP = {
  ETHEREUM: ['ETHEREUM', 'ETHEREUM_TEST', 'BSC', 'BSC_TEST', 'POLYGON', 'POLYGON_TEST'],
  FLOW: ['FLOW', 'FLOW_TEST'],
  NEO: ['NEO', 'NEO_TEST'],
  SOLANA: ['SOLANA', 'SOLANA_TEST'],
  NEAR: ['NEAR', 'NEAR_TEST']
}

@Component({
  selector: 'app-omni-chain-contracts',
  templateUrl: './omni-chain-contracts.component.html',
  styleUrls: ['./omni-chain-contracts.component.css']
})
export class OmniChainContractsComponent implements OnInit {

  @ViewChild(MatPaginator) paginator: MatPaginator;
  displayedColumns: string[] = [
    'select',
    'id',
    'name',
    'displayName',
    'vaultName',
    'vaultId',
    'apis',
    'editAction',
    'removeAction',
  ];
  apis = [
    { value: 'any', label: 'Any' },
    { value: 'ETHEREUM', label: 'ETHEREUM' },
    { value: 'FLOW', label: 'FLOW' },
    { value: 'NEO', label: 'NEO' },
    { value: 'SOLANA', label: 'SOLANA' },
    { value: 'NEAR', label: 'NEAR' },
  ];
  networks = [
    { value: 'any', label: 'Any' },
    { value: 'BSC', label: 'BSC' },
    { value: 'BSC_TEST', label: 'BSC_TEST' },
    { value: 'ETHEREUM', label: 'ETHEREUM' },
    { value: 'ETHEREUM_TEST', label: 'ETHEREUM_TEST' },
    { value: 'FLOW', label: 'FLOW' },
    { value: 'FLOW_TEST', label: 'FLOW_TEST' },
    { value: 'NEO', label: 'NEO' },
    { value: 'NEO_TEST', label: 'NEO_TEST' },
    { value: 'POLYGON', label: 'POLYGON' },
    { value: 'POLYGON_TEST', label: 'POLYGON_TEST' },
    { value: 'SOLANA', label: 'SOLANA'  },
    { value: 'SOLANA_TEST', label: 'SOLANA_TEST' },
    { value: 'NEAR', label: 'NEAR'  },
    { value: 'NEAR_TEST', label: ' NEAR_TEST' },
  ];
  api = '';
  network: string[] = [];
  dataSource: ContractsDataSource;
  hasSelection = false;
  selection: SelectionModel<Contract>;
  contracts: Contract[] = [];
  userType: string | null = null;

  constructor(
    public dialog: MatDialog,
    private contractsService: ContractsService,
    private dialogService: ConfirmationDialogService,
  ) { }

  ngOnInit(): void {
    this.selection = new SelectionModel<Contract>(true, []);
    this.dataSource = new ContractsDataSource(this.contractsService);
    this.refresh();
  }

  ngAfterViewInit(): void {
    this.paginator.page
      .pipe(
        tap(() => this.refresh())
      )
      .subscribe();
    this.selection.changed.subscribe(s => this.hasSelection = this.selection.hasValue());
    this.dataSource.contracts$.subscribe(contracts => this.contracts = contracts);
    this.dataSource.totalCount$.subscribe(totalCount => this.paginator.length = totalCount);
  }

  getApis(addresses: ContractAddresses) {
    const apis = [];
    const addressesKeys = Object.keys(addresses);
    for (const key in API_NETWORK_MAP) {
      for (let i = 0; i < addressesKeys.length; i++) {
        if (API_NETWORK_MAP[key].includes(addressesKeys[i]) && !apis.includes(key)) {
          apis.push(key);
        }
      }
    }
    return apis.join(', ');
  }

  changeNetwork(value: string) {
    if (value === 'any') {
      this.network = [];
    } else {
      this.network = [value];
    }
    this.refresh();
  }

  changeApi(value: string) {
    if (value === 'any') {
      this.api = '';
    } else {
      this.api = value;
    }
    this.refresh();
  }

  changeUserType(value: string) {
    this.userType = value;
    this.refresh();
  }

  isAllSelected() {
    const numSelected = this.selection.selected.length;
    const numRows = this.contracts.length;
    return numSelected == numRows;
  }

  masterToggle() {
    this.isAllSelected() ?
      this.selection.clear() :
      this.contracts.forEach(row => this.selection.select(row));
  }

  openNewContractDialog() {
    this.dialog.open(OmniChainContractsDialogComponent, {
      panelClass: 'modal-container',
      data: {
        refresher: this,
      },
    });
  }

  openEditContractDialog(contract: Contract) {
    this.dialog.open(OmniChainContractsDialogComponent, {
      panelClass: 'modal-container',
      data: {
        contract,
        refresher: this,
      },
    });
  }

  deleteContract(contract: Contract) {
    this.dialogService
      .confirm(
        "Confirm Dialog",
        `Are you sure you want to delete ${contract.displayName}? Deletion is permanent.`
      )
      .pipe(filter((r) => r))
      .subscribe(() => {
        this.contractsService.deleteContract(contract.id).subscribe(() => {
          this.refresh();
        });
      });
  }

  deleteSelectedContracts() {
    this.dialogService
      .confirm('Confirm Dialog', `Are you sure you want to delete the ${this.selection.selected.length} selected contract${this.selection.selected.length == 1 ? '' : 's'}? Deletion is permanent.`)
      .pipe(filter(r => r))
      .subscribe(() => {
        this.selection.selected.forEach(vault => {
          this.contractsService.deleteContract(vault.id).subscribe(() => {
            this.refresh();
          });
        });
        this.selection.clear();
        this.refresh();
      });
  }

  refresh(delay = 0) {
    setTimeout(() => {
      this.selection.clear();
      this.dataSource.loadContracts(
        this.paginator.pageIndex * this.paginator.pageSize,
        this.paginator.pageSize,
        this.api || null,
        this.network.length === 0 ? null : this.network,
        this.userType,
      );
    }, delay);
  }
}
