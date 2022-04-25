import { CdkDragDrop } from '@angular/cdk/drag-drop';
import { Component, Inject, OnInit } from '@angular/core';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { TokenSpecTab, TokenSpecTabField, TokenSpecTabFieldTypes } from '../api/models/blockchain/token-spec-tab';

import { NeoSmartContractsService } from '../api/services/blockchain/neo-smart-contracts.service';
import { NeoTokenSpecsService } from '../api/services/blockchain/neo-token-specs.service';
import { NeoSmartTokenSpecsMoveFieldDialogComponent } from '../neo-smart-token-specs-move-field-dialog/neo-smart-token-specs-move-field-dialog.component';
import { NeoTokenDialogDefineObjectComponent } from '../neo-token-dialog-define-object/neo-token-dialog-define-object.component';

export enum FieldTypes {
  STRING = 'String',
  NUMBER = 'Number',
  BOOLEAN = 'Boolean',
  ENUM = 'Enum',
  OBJECT = 'Object',
  TAGS = 'Tags',
  ARRAY = 'Array',
}

export interface TabType {
  key: string;
  value: FieldTypes;
}

export interface TabField {
  name: string;
  type: FieldTypes;
  content: string;
}

interface Contract {
  id: string;
  displayName: string;
}

@Component({
  selector: 'app-neo-smart-token-specs-dialog',
  templateUrl: './neo-smart-token-specs-dialog.component.html',
  styleUrls: ['./neo-smart-token-specs-dialog.component.css']
})
export class NeoSmartTokenSpecsDialogComponent implements OnInit {
  tokenName: string = '';
  selectedContract: string = '';
  maxTabs = 5;
  tabs: TokenSpecTab[] = [this.createTab()];
  fields: TokenSpecTabField[] = [];
  contracts: Contract[] = [];
  tabTypes: TabType[] = [];
  activeTabIndex = 0;
  activeFieldIndex = null;
  // Workaround for accordion animation on init
  disableAnimation = true;

  constructor(
    private neoSmartContractsService: NeoSmartContractsService,
    private neoTokenSpecsService: NeoTokenSpecsService,
    public dialogRef: MatDialogRef<NeoSmartTokenSpecsDialogComponent>,
    public dialog: MatDialog,
    @Inject(MAT_DIALOG_DATA)
    public data: {
      tokenSpec: TokenSpecTab,
    },
  ) { }

  ngOnInit(): void {
    this.neoSmartContractsService.getNeoSmartContracts({})
      .subscribe((res) => {
        this.contracts = res.objects;
      });
    this.tabTypes = Object.keys(FieldTypes).map(key => ({
      key,
      value: FieldTypes[key]
    }));
    this.fields = this.activeTab?.fields || [this.createField()];
  }

  ngAfterViewInit(): void {
    // timeout required to avoid the dreaded 'ExpressionChangedAfterItHasBeenCheckedError'
    setTimeout(() => this.disableAnimation = false);
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
      name: "",
      type: TokenSpecTabFieldTypes.STRING,
      content: "",
    };
  }

  selectTab(tabIndex) {
    this.updateActiveTabFields(this.fields);
    this.activeTabIndex = tabIndex;
    this.fields = this.tabs[tabIndex]?.fields || [];
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
    this.fields = this.fields.map(
      (field: TokenSpecTabField, index: number): TokenSpecTabField => {
        if (index === fieldIndex) {
          return {
            ...field,
            type: FieldTypes[key],
          }
        }
        return field;
      }
    );
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

  openDefineObjectModal(index) {
    this.dialog.open(NeoTokenDialogDefineObjectComponent, {
      width: "800px",
      data: {
        updateFieldsWithContent: this.updateFieldsWithContent.bind(this),
      }
    });
    this.activeFieldIndex = index;
  }

  changeFieldTab(fieldIndex: number, newTabIndex: number) {
    let field;
    const newFields = this.activeTab?.fields?.filter(
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
      this.tabs = [...this.tabs, this.activeTab];
      this.selectTab(this.tabs.length - 1);
    }
  }

  updateTabName(value: string): void {
    this.tabs = this.tabs.map((tab: TokenSpecTab, index: number): TokenSpecTab => {
      if (index === this.activeTabIndex) {
        return {
          ...tab,
          name: value,
        }
      }
      return tab;
    });
  }

  updateTabPosition(newTabIndex: string): void {
    const tabIndex = parseInt(newTabIndex) - 1;
    const tab = { ...this.activeTab };
    this.tabs[this.activeTabIndex] = this.tabs[tabIndex];
    this.tabs[tabIndex] = tab;
  }

  removeTab(): void {
    this.tabs = this.tabs.filter((_, index: number) =>
      index !== this.activeTabIndex,
    );
  }

  //

  updateFieldsWithContent(data) {
    this.fields = this.fields.map(
      (field: TokenSpecTabField, index: number) => {
        if (index === this.activeFieldIndex) {
          return {
            ...field,
            content: data,
          }
        }
        return field;
      }
    );
  }

  close() {
    this.dialogRef.close();
  }

  submit() {
    this.close();
    this.updateActiveTabFields(this.fields);
    this.neoTokenSpecsService.createTokenSpec({
      tokenName: this.tokenName,
      contractId: this.selectedContract,
      tabs: this.tabs,
    });
  }
}
