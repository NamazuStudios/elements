import { CdkDragDrop, moveItemInArray } from '@angular/cdk/drag-drop';
import { Component, OnInit } from '@angular/core';

import { NeoSmartContractsService } from '../api/services/blockchain/neo-smart-contracts.service';

export enum TabTypes {
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
  value: TabTypes;
}

interface TabField {
  name: string;
  type: TabTypes;
  content: string;
}

interface Tab {
  name: string;
  fields: TabField[];
}

@Component({
  selector: 'app-neo-smart-token-specs-dialog',
  templateUrl: './neo-smart-token-specs-dialog.component.html',
  styleUrls: ['./neo-smart-token-specs-dialog.component.css']
})
export class NeoSmartTokenSpecsDialogComponent implements OnInit {
  maxTabs = 5;
  tabs: Tab[] = [this.createTab()];
  contracts = [
    { key: 1, toolTip: 'ContractA', label: 'ContractA' },
    { key: 2, toolTip: 'ContractB', label: 'ContractB' },
    { key: 3, toolTip: 'ContractC', label: 'ContractC' },
  ];
  activeTabType = { key: 'STRING', value: TabTypes.STRING };
  tabTypes: TabType[] = [];
  activeTabIndex = 0;

  constructor(
    private neoSmartContractsService: NeoSmartContractsService
  ) { }

  ngOnInit(): void {
    this.neoSmartContractsService.getNeoSmartContracts({})
      .subscribe((tokens) => {
        console.log(tokens);
      });
    this.tabTypes = Object.keys(TabTypes).map(key => ({
      key,
      value: TabTypes[key]
    }));
  }

  get activeTab(): Tab {
    return this.tabs[this.activeTabIndex];
  }

  createTab(): Tab {
    return {
      name: '',
      fields: [this.createField()],
    };
  }

  createField(): TabField {
    return {
      name: "",
      type: TabTypes.STRING,
      content: "",
    };
  }

  selectTab(tabIndex) {
    this.activeTabIndex = tabIndex;
  }

  selectType(val) {
    this.activeTabType = {
      key: val,
      value: TabTypes[val]
    };
  }

  submit() { }

  addNewTab() {
    if (this.tabs.length < this.maxTabs) {
      this.tabs = [...this.tabs, this.createTab()];
      this.selectTab(this.tabs.length - 1);
    }
  }

  addNewField() {
    this.tabs = this.tabs.map((tab: Tab, index: number): Tab => {
      if (index === this.activeTabIndex) {
        return {
          ...tab,
          fields: [
            ...tab.fields,
            this.createField(),
          ]
        };
      }
      return tab;
    });
  }

  changeFieldName(value: string, fieldIndex: number) {
    if (this.activeTab) {
      const updatedTab = {
        ...this.activeTab,
        fields: this.activeTab.fields.map((field, index) => {
          if (fieldIndex === index) {
            return {
              ...field,
              name: value,
            }
          }
          return field;
        }),
      };
      this.tabs = this.tabs.map((tab: Tab, index: number) => {
        if (index === this.activeTabIndex) {
          return updatedTab;
        }
        return tab;
      });
    }
  }

  updateFields(newFields: TabField[]) {
    this.tabs = this.tabs.map((tab: Tab, index: number) => {
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
    const fields = [...this.activeTab.fields];
    const currentField = { ...fields[event.previousIndex] };
    const fieldToReplace = { ...fields[event.currentIndex] };
    fields[event.previousIndex] = fieldToReplace;
    fields[event.currentIndex] = currentField;
    this.updateFields(fields);
  }

  removeField(fieldIndex: number) {
    const fields = this.activeTab.fields.filter(
      (_: TabField, index: number): boolean => {
        return fieldIndex !== index;
      }
    );
    this.updateFields(fields);
  }

  duplicateField(fieldIndex: number) {
    const fields = [...this.activeTab.fields, this.activeTab.fields[fieldIndex]];
    this.updateFields(fields);
  }
}
