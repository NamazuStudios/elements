import { COMMA, ENTER } from '@angular/cdk/keycodes';
import { Component, Inject, OnInit } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';

import { NeoSmartContract } from '../api/models/blockchain/neo-smart-contract';
import { TokenDefinition } from '../api/models/blockchain/token-definition';
import { TokenSpecTab, TokenSpecTabField, TokenSpecTabFieldTypes, TokenTemplate } from '../api/models/token-spec-tab';
import { NeoSmartContractsService } from '../api/services/blockchain/neo-smart-contracts.service';
import { TokenDefinitionService } from '../api/services/blockchain/token-definition.service';
import { MetadataSpecsService } from '../api/services/metadata-specs.service';
import { AuthenticationService } from '../authentication.service';
import { NeoSmartContractsDataSource } from '../neo-smart-contracts.datasource';
import { NeoTokensSpecDataSource } from '../neo-tokens-spec.datasource';

const complexFields = [
  TokenSpecTabFieldTypes.OBJECT,
  TokenSpecTabFieldTypes.ARRAY,
  TokenSpecTabFieldTypes.BOOLEAN,
];

@Component({
  selector: 'app-neo-token-dialog-updated',
  templateUrl: './neo-token-dialog-updated.component.html',
  styleUrls: ['./neo-token-dialog-updated.component.css']
})
export class NeoTokenDialogUpdatedComponent implements OnInit {

  readonly separatorKeysCodes = [ENTER, COMMA ] as const;
  contractsDataSource: NeoSmartContractsDataSource;
  tokenSpecsDataSource: NeoTokensSpecDataSource;
  name = '';
  activeTabIndex = 0;
  fields: TokenSpecTabField[] = [];
  contracts: NeoSmartContract[] = [];
  templates: TokenTemplate[] = [];
  selectedContract: NeoSmartContract;
  selectedTemplate: TokenTemplate;
  tabs: TokenSpecTab[];
  // Workaround for accordion animation on init
  disableAnimation = true;

  constructor(
    private neoSmartContractsService: NeoSmartContractsService,
    private metadataSpecsService: MetadataSpecsService,
    private tokenDefinitionService: TokenDefinitionService,
    private authenticationService: AuthenticationService,
    public dialogRef: MatDialogRef<NeoTokenDialogUpdatedComponent>,
    @Inject(MAT_DIALOG_DATA)
    public data: {
      token: TokenDefinition,
      template: TokenTemplate,
      contract: NeoSmartContract,
      contracts: NeoSmartContract[],
      templates: TokenTemplate[],
      refresher;
      close;
    },
  ) { }

  ngOnInit(): void {
    this.contractsDataSource = new NeoSmartContractsDataSource(this.neoSmartContractsService);
    this.contractsDataSource.loadNeoSmartContracts(null, null, null);
    this.tokenSpecsDataSource = new NeoTokensSpecDataSource(this.metadataSpecsService);
    this.tokenSpecsDataSource.loadTemplates(null, null);
    if (this.data.token?.id) {
      this.tabs = this.data?.token?.metadata?.tabs.map((tab) => {
        const fields = tab.fields.map((field) => {
          if (field?.fieldType === 'Array' || field?.fieldType === 'Object') {
            return {
              ...field,
              content: field.value,
            }
          }
          return field;
        });
        return {
          ...tab,
          fields,
        }
      });
      this.name = this.data.token.name;
      this.selectedContract = this.data.token.contract;
      this.selectedTemplate = this.data.token.metadataSpec;
    } else {
      this.tabs = this.selectedTemplate?.tabs.map(tab => ({
        ...tab,
        fields: this.convertFieldsToArray(tab.fields),
      }));
    }
    this.selectTab(0);
  }

  ngAfterViewInit(): void {
    // timeout required to avoid the dreaded 'ExpressionChangedAfterItHasBeenCheckedError'
    setTimeout(() => this.disableAnimation = false);

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

  selectTemplate(tempalteId: string): void {
    const template = this.templates.find((template: TokenTemplate): boolean =>
      template.id === tempalteId,
    );
    this.selectedTemplate = template;
    this.tabs = template.tabs.map(tab => ({
      ...tab,
      fields: this.convertFieldsToArray(tab.fields),
    }));
    this.fields = [];
    this.selectTab(0);
  }

  getFieldValue(field: TokenSpecTabField) {
    switch(field.fieldType) {
      case 'Tags': {
        if (field.defaultValue) {
          return field.defaultValue.split(',');
        }
        break;
      }
      case 'Number':
        return field.defaultValue;
      default:
        return '';
    }
  }

  convertFieldsToArray(fields): TokenSpecTabField[] {
    if (fields?.length !== undefined) return fields;
    const keys = Object.keys(fields);
    const newFields: TokenSpecTabField[] = [];
    for (let i = 0; i < keys.length; i++) {
      const field = fields[keys[i]];
      console.log(field);
      newFields.push({
        name: field?.name || '',
        fieldType: field.fieldType,
        placeHolder: field.placeHolder,
        value: this.getFieldValue(field),
        content:
          complexFields.includes(field.fieldType) && field.defaultValue
            ? JSON.parse(field.defaultValue)
            : field.defaultValue,
      });
    }
    return newFields;
  }

  changeName(value: string) {
    this.name = value;
  }

  selectTab(tabIndex) {
    if (this.fields.length > 0) {
      this.updateActiveTabFields(this.fields);
    }
    this.activeTabIndex = tabIndex;
    this.fields = this.tabs[tabIndex].fields;
  }

  updateActiveTabFields(newFields: TokenSpecTabField[]) {
    this.tabs = this.tabs.map((tab: TokenSpecTab, index: number) => {
      if (index === this.activeTabIndex) {
        return {
          ...tab,
          fields: newFields,
        };
      }
      return tab;
    });
  }

  changeFieldValue(value: string, fieldIndex: number) {
    this.fields = this.fields.map((field, index) => {
      if (fieldIndex === index) {
        return {
          ...field,
          value: value,
        }
      }
      return field;
    });
  }

  submit() {
    const session = this.authenticationService.currentSession;
    this.updateActiveTabFields(this.fields);
    const data = {
      name: this.name,
      displayName: this.name,
      metadataSpecId: this.selectedTemplate.id,
      contractId: this.selectedContract.id,
      userId: session.session.user.id,
      metadata: {
        tabs: this.tabs,
      },
    }
    if (this.data.token?.id) {
      this.tokenDefinitionService.updateTokenDefinition({
        id: this.data.token.id,
        body: data,
      }).subscribe(() => {
        this.data.refresher.refresh();
      });
    } else {
      this.tokenDefinitionService.createTokenDefinition(data)
        .subscribe(() => {
          this.data.refresher.refresh();
        });
    }
    this.close();
  }

  close() {
    this.dialogRef.close();
  }
}
