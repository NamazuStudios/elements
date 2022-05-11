import { COMMA, ENTER } from '@angular/cdk/keycodes';
import { Component, Inject, OnInit } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';

import { NeoSmartContract } from '../api/models/blockchain/neo-smart-contract';
import { TokenSpecTabField, TokenSpecTabFieldTypes, TokenTemplate } from '../api/models/token-spec-tab';

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
  name = '';
  activeTabIndex = 0;
  fields: TokenSpecTabField[] = [];

  constructor(
    public dialogRef: MatDialogRef<NeoTokenDialogUpdatedComponent>,
    @Inject(MAT_DIALOG_DATA)
    public data: {
      template: TokenTemplate,
      contract: NeoSmartContract,
    },
  ) { }

  ngOnInit(): void {
    this.selectTab(0);
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
    const tab = this.data.template.tabs[tabIndex];
    this.fields = this.convertFieldsToArray(tab.fields);
    this.activeTabIndex = tabIndex;
  }

  close() {
    this.dialogRef.close();
  }
}
