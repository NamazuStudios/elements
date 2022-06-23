import { CdkDragDrop } from '@angular/cdk/drag-drop';
import { Component, Inject, OnInit } from '@angular/core';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { NeoSmartContract } from '../api/models/blockchain/neo-smart-contract';
import { TokenSpecTab, TokenSpecTabField, TokenSpecTabFieldTypes, TokenTemplate } from '../api/models/token-spec-tab';

import { NeoSmartContractsService } from '../api/services/blockchain/neo-smart-contracts.service';
import { MetadataSpecsService } from '../api/services/metadata-specs.service';
import { NeoSmartContractsDataSource } from '../neo-smart-contracts.datasource';
import { NeoSmartTokenSpecsMoveFieldDialogComponent } from '../neo-smart-token-specs-move-field-dialog/neo-smart-token-specs-move-field-dialog.component';
import { NeoTokenDialogDefineObjectComponent } from '../neo-token-dialog-define-object/neo-token-dialog-define-object.component';

interface TabType {
  key: string;
  value: TokenSpecTabFieldTypes;
}

const complexFields = [
  TokenSpecTabFieldTypes.OBJECT,
  TokenSpecTabFieldTypes.ARRAY,
];

export const enumRegex = /^[a-zA-Z0-9_]+(,[a-zA-Z0-9_]+)*$/;

@Component({
  selector: 'app-neo-smart-token-specs-dialog',
  templateUrl: './neo-smart-token-specs-dialog.component.html',
  styleUrls: ['./neo-smart-token-specs-dialog.component.css']
})
export class NeoSmartTokenSpecsDialogComponent implements OnInit {
  contractsDataSource: NeoSmartContractsDataSource;
  tokenName: string = '';
  selectedContract: string = '';
  maxTabs = 5;
  tabs: TokenSpecTab[] = [this.createTab()];
  tabName: string = '';
  fields: TokenSpecTabField[] = [];
  contracts: NeoSmartContract[] = [];
  tabTypes: TabType[] = [];
  activeTabIndex = 0;
  activeFieldIndex = null;
  expandedField = null;
  // Workaround for accordion animation on init
  disableAnimation = true;

  constructor(
    private neoSmartContractsService: NeoSmartContractsService,
    private metadataSpecsService: MetadataSpecsService,
    public dialogRef: MatDialogRef<NeoSmartTokenSpecsDialogComponent>,
    public dialog: MatDialog,
    @Inject(MAT_DIALOG_DATA)
    public data: {
      template: TokenTemplate,
      refresh: Function,
    },
  ) { }

  ngOnInit(): void {
    this.contractsDataSource = new NeoSmartContractsDataSource(this.neoSmartContractsService);
    this.contractsDataSource.loadNeoSmartContracts(null, null, null);
    this.tabTypes = Object.keys(TokenSpecTabFieldTypes).map(key => ({
      key,
      value: TokenSpecTabFieldTypes[key]
    }));
    if (this.data.template) {
      const { name, contractId, tabs } = this.data.template;
      this.tokenName = name;
      this.selectedContract = contractId;
      this.tabs = tabs.map(tab => ({
        ...tab,
        fields: this.convertFieldsToArray(tab.fields),
      }));
    }
    this.fields = this.activeTab?.fields || [this.createField()];
    this.tabName = this.activeTab?.name || '';
  }

  ngAfterViewInit(): void {
    // timeout required to avoid the dreaded 'ExpressionChangedAfterItHasBeenCheckedError'
    setTimeout(() => this.disableAnimation = false);
    this.contractsDataSource.neoSmartContracts$.subscribe(
      (currentNeoSmartContracts) => (this.contracts = currentNeoSmartContracts)
    );
  }

  get activeTab(): TokenSpecTab {
    return this.tabs[this.activeTabIndex];
  }

  createTab(): TokenSpecTab {
    return {
      name: '',
      fields: [this.createField()],
    };
  }

  createField(): TokenSpecTabField {
    return {
      name: '',
      fieldType: TokenSpecTabFieldTypes.STRING,
      content: '',
    };
  }

  convertFieldsToArray(fields): TokenSpecTabField[] {
    if (fields?.length !== undefined) return fields;
    const keys = Object.keys(fields);
    const newFields: TokenSpecTabField[] = [];
    for (let i = 0; i < keys.length; i++) {
      const field = fields[keys[i]];
      newFields.push({
        name: field?.name || '',
        fieldType: field.fieldType,
        placeHolder: field.placeHolder,
        content: complexFields.includes(field.fieldType) ? JSON.parse(field.defaultValue) : field.defaultValue,
        defaultValue: complexFields.includes(field.fieldType) ? JSON.parse(field.defaultValue) : field.defaultValue
      });
    }
    return newFields;
  }

  convertFieldsToObject(fields: TokenSpecTabField[]) {
    if (fields?.length === undefined) return fields;
    const newFields = {};
    for (let i = 0; i < fields.length; i++) {
      const field = fields[i];
      let defaultValue;
      if (field.defaultValue) {
        if (complexFields.includes(field.fieldType)) {
          defaultValue = JSON.stringify(field.defaultValue);
        } else {
          defaultValue = field.defaultValue;
        }
      } else if (field?.content) {
        if (complexFields.includes(field.fieldType)) {
          defaultValue = JSON.stringify(field.content);
        } else {
          defaultValue = field.content;
        }
      }
      newFields[i] = {
        name: field.name,
        displayName: field.name,
        fieldType: field.fieldType,
        placeHolder: field.placeHolder,
        defaultValue,
      }
    };
    return newFields;
  }

  selectTab(tabIndex) {
    this.updateActiveTabFields(this.fields);
    this.updateActiveTabName(this.tabName);
    this.disableAnimation = true;
    this.activeTabIndex = tabIndex;
    this.fields = this.tabs[tabIndex]?.fields || [];
    this.tabName = this.tabs[tabIndex]?.name || '';
    this.expandedField = null;
    setTimeout(() => this.disableAnimation = false);
  }

  addNewTab() {
    if (this.tabs.length < this.maxTabs) {
      this.tabs = [...this.tabs, this.createTab()];
      this.selectTab(this.tabs.length - 1);
    }
  }

  addNewField() {
    this.fields = [...this.fields, this.createField()];
  }

  handleFieldPanelStateChange(index: number) {
    this.expandedField = index;
  }

  changeFieldName(value: string, fieldIndex: number) {
    this.fields = this.fields.map((field, index) => {
      if (fieldIndex === index) {
        return {
          ...field,
          name: value,
        }
      }
      return field;
    });
  }

  changeFieldType(key: string, fieldIndex: number) {
    this.disableAnimation = true
    this.fields = this.fields.map(
      (field: TokenSpecTabField, index: number): TokenSpecTabField => {
        if (index === fieldIndex) {
          return {
            ...field,
            fieldType: TokenSpecTabFieldTypes[key],
          }
        }
        return field;
      }
    );
    setTimeout(() => this.disableAnimation = false);
  }

  changeFieldContentType(type: string, fieldIndex): void {
    this.disableAnimation = true
    this.fields = this.fields.map(
      (field: TokenSpecTabField, index: number): TokenSpecTabField => {
        if (index === fieldIndex) {
          return {
            ...field,
            fieldContentType: type,
          }
        }
        return field;
      }
    );
    setTimeout(() => this.disableAnimation = false);
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

  drop(event: CdkDragDrop<string[]>) {
    const fields = [...this.fields];
    const currentField = { ...fields[event.previousIndex] };
    const fieldToReplace = { ...fields[event.currentIndex] };
    fields[event.previousIndex] = fieldToReplace;
    fields[event.currentIndex] = currentField;
    this.fields = fields;
  }

  removeField(fieldIndex: number) {
    this.fields = this.fields.filter(
      (_: TokenSpecTabField, index: number): boolean => {
        return fieldIndex !== index;
      }
    );
  }

  duplicateField(fieldIndex: number) {
    this.fields = [...this.fields, this.fields[fieldIndex]];
  }

  selectContract(contractId: string) {
    this.selectedContract = contractId;
  }

  changeTokenName(value: string) {
    this.tokenName = value;
  }

  openDefineObjectModal(index: number): void {
    this.dialog.open(NeoTokenDialogDefineObjectComponent, {
      width: "800px",
      data: {
        updateFieldsWithContent: this.updateFieldsWithContent.bind(this),
        content: this.fields[index]?.content,
      }
    });
    this.activeFieldIndex = index;
  }

  changeFieldTab(fieldIndex: number, newTabIndex: number) {
    let field;
    const newFields = this.fields?.filter(
      (f: TokenSpecTabField, index: number): boolean => {
        if (index === fieldIndex) {
          field = f;
        }
        return index !== fieldIndex;
      }
    );
    this.fields = newFields;
    this.tabs = this.tabs.map((tab: TokenSpecTab, index: number): TokenSpecTab => {
      if (index === (newTabIndex - 1) && field) {
        return {
          ...tab,
          fields: [
            ...tab.fields,
            field,
          ]
        }
      }
      return tab;
    });
  }

  moveFieldToAnotherTab(index: number) {
    this.dialog.open(NeoSmartTokenSpecsMoveFieldDialogComponent, {
      width: "500px",
      data: {
        fieldIndex: index,
        activeTabIndex: this.activeTabIndex + 1,
        max: this.tabs.length,
        changeFieldTab: this.changeFieldTab.bind(this),
      }
    });
    this.activeFieldIndex = index;
  }

  // Tab actions

  duplicateTab() {
    if (this.activeTab && this.tabs.length < this.maxTabs) {
      const tab = { ...this.activeTab, fields: this.fields, name: this.tabName };
      this.tabs = [...this.tabs, tab];
      this.selectTab(this.tabs.length - 1);
    }
  }

  updateActiveTabName(value: string): void {
    this.tabs = this.tabs.map((tab: TokenSpecTab, index: number): TokenSpecTab => {
      if (index === this.activeTabIndex) {
        return {
          ...tab,
          name: value,
        };
      }
      return tab;
    });
  }

  updateTabName(value: string): void {
    this.tabName = value;
  }

  updateTabPosition(newTabIndex: string): void {
    if (+newTabIndex <= this.tabs.length) {
      const tabIndex = parseInt(newTabIndex) - 1;
      const tab1 = { ...this.activeTab };
      const tab2 = { ...this.tabs[tabIndex] };
      this.tabs[this.activeTabIndex] = tab2;
      this.tabs[tabIndex] = tab1;
      this.fields = tab2.fields;
      this.tabName = tab2.name;
    }
  }

  removeTab(): void {
    this.tabs = this.tabs.filter((_, index: number) =>
      index !== this.activeTabIndex,
    );
  }

  //

  updateFieldsWithContent(data) {
    const fieldIndex = data.index || data.index === 0 ? data.index : this.activeFieldIndex;
    this.fields = this.fields.map(
      (field: TokenSpecTabField, index: number) => {
        if (index === fieldIndex) {
          const content = data.hasOwnProperty('content') ? data.content : data;
          return {
            ...field,
            content: content && !content?.otherProps ? content : field.content,
            placeHolder: data?.otherProps?.placeHolder || field.placeHolder || '',
            defaultValue: data?.otherProps?.defaultValue || field.defaultValue || '',
          }
        }
        return field;
      }
    );
  }

  isValid(): boolean {
    let isValid = true;
    if (!this.tokenName) {
      isValid = false;
    }

    this.tabs.forEach((tab, index) => {
      const fields = index === this.activeTabIndex ? this.fields : tab.fields;
      for (let i = 0; i < fields.length; i++) {
        const field = fields[i];
        if (field?.fieldType === TokenSpecTabFieldTypes.ENUM && !enumRegex.test(field.content)) {
          isValid = false;
        }
      }
    });
    return isValid;
  }

  close() {
    this.dialogRef.close();
  }

  async submit() {
    this.close();
    this.updateActiveTabFields(this.fields);
    this.updateActiveTabName(this.tabName);

    // Convert body for the api format
    const body = {
      name: this.tokenName,
      tabs: this.tabs.map(tab => ({
        ...tab,
        fields: this.convertFieldsToObject(tab.fields),
      })),
    };

    if (this.data.template) {
      this.metadataSpecsService.updateTokenTemplate({
        id: this.data.template.id,
        body,
      })
        .subscribe(() => {
          this.data.refresh();
        });;
    } else {
      this.metadataSpecsService.createTokenSpec(body)
        .subscribe(() => {
          this.data.refresh();
        });
    }
  }
}
