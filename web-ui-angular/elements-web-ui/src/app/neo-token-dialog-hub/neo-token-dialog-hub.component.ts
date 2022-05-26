import { Component, Inject, OnInit } from '@angular/core';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';

import { NeoSmartContract } from '../api/models/blockchain/neo-smart-contract';
import { TokenDefinition } from '../api/models/blockchain/token-definition';
import { TokenTemplate } from '../api/models/token-spec-tab';
import { NeoSmartContractsService } from '../api/services/blockchain/neo-smart-contracts.service';
import { MetadataSpecsService } from '../api/services/metadata-specs.service';
import { NeoSmartContractsDataSource } from '../neo-smart-contracts.datasource';
import { NeoTokenDialogUpdatedComponent } from '../neo-token-dialog-updated/neo-token-dialog-updated.component';
import { NeoTokenDialogComponent } from '../neo-token-dialog/neo-token-dialog.component';
import { NeoTokensSpecDataSource } from '../neo-tokens-spec.datasource';

@Component({
  selector: 'app-neo-token-dialog-hub',
  templateUrl: './neo-token-dialog-hub.component.html',
  styleUrls: ['./neo-token-dialog-hub.component.css']
})
export class NeoTokenDialogHubComponent implements OnInit {

  contractsDataSource: NeoSmartContractsDataSource;
  tokenSpecsDataSource: NeoTokensSpecDataSource;
  contracts: NeoSmartContract[] = [];
  templates: TokenTemplate[] = [];
  selectedContract: NeoSmartContract;
  selectedTempalte: TokenTemplate;
  error = false;

  constructor(
    private neoSmartContractsService: NeoSmartContractsService,
    private metadataSpecsService: MetadataSpecsService,
    public dialogRef: MatDialogRef<NeoTokenDialogHubComponent>,
    @Inject(MAT_DIALOG_DATA)
    public data: {
      isNew: boolean;
      token: TokenDefinition;
      next: any;
      refresher: any;
    },
    public dialog: MatDialog,
  ) { }

  ngOnInit(): void {
    if (this.data.token?.id) {
      this.selectedContract = this.data.token.contract;
      this.selectedTempalte = this.data.token.metadataSpec;
    }
    this.contractsDataSource = new NeoSmartContractsDataSource(this.neoSmartContractsService);
    this.contractsDataSource.loadNeoSmartContracts(null, null, null);
    this.tokenSpecsDataSource = new NeoTokensSpecDataSource(this.metadataSpecsService);
    this.tokenSpecsDataSource.loadTemplates(null, null);
  }

  ngAfterViewInit(): void {
    this.contractsDataSource.neoSmartContracts$.subscribe(
      (currentNeoSmartContracts: NeoSmartContract[]) => (this.contracts = currentNeoSmartContracts)
    );
    this.tokenSpecsDataSource.tokens$.subscribe(
      (templates: TokenTemplate[]): void => {
        this.templates = templates;
      }
    );
  }

  selectContract(contractId: string): void {
    const contract = this.contracts.find((contract: NeoSmartContract): boolean =>
      contract.id === contractId,
    );
    this.selectedContract = contract;
  }

  selectTokenTempalte(tempalteId: string): void {
    const template = this.templates.find((template: TokenTemplate): boolean =>
      template.id === tempalteId,
    );
    this.selectedTempalte = template;
  }

  openNewNeoTokenDialog(): void {
    if ((this.selectedContract && this.selectedTempalte) || this.data.token?.id) {
      this.dialog.open(NeoTokenDialogUpdatedComponent, {
        width: "850px",
        maxHeight: "90vh",
        data: {
          token: this.data.token,
          contract: this.selectedContract,
          template: this.selectedTempalte,
          contracts: this.contracts,
          templates: this.templates,
          refresher: this.data.refresher,
          selectContract: this.selectContract.bind(this),
          selectTemplate: this.selectTokenTempalte.bind(this),
          close: this.close.bind(this),
        },
      });
    }
    else {
      this.error = true;
    }
  }

  openLegacyNeoTokenDialog(): void {
    this.dialog.open(NeoTokenDialogComponent, {
      width: "850px",
      data: this.data,
    });
  }

  close(): void {
    this.dialogRef.close();
  }
}
