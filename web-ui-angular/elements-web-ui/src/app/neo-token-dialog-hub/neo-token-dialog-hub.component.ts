import { Component, Inject, OnInit } from '@angular/core';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { NeoToken } from '../api/models/blockchain/neo-token';
import { NeoTokenDialogUpdatedComponent } from '../neo-token-dialog-updated/neo-token-dialog-updated.component';
import { NeoTokenDialogComponent } from '../neo-token-dialog/neo-token-dialog.component';

@Component({
  selector: 'app-neo-token-dialog-hub',
  templateUrl: './neo-token-dialog-hub.component.html',
  styleUrls: ['./neo-token-dialog-hub.component.css']
})
export class NeoTokenDialogHubComponent implements OnInit {

  contracts = [
    { key: 1, toolTip: 'ContractA', label: 'ContractA' },
    { key: 2, toolTip: 'ContractB', label: 'ContractB' },
    { key: 3, toolTip: 'ContractC', label: 'ContractC' },
  ];

  tokenSpecs = [
    { key: 1, toolTip: 'TokenSpec1', label: 'TokenSpec1' },
    { key: 2, toolTip: 'TokenSpec2', label: 'TokenSpec2' },
    { key: 3, toolTip: 'TokenSpec3', label: 'TokenSpec3' },
  ];

  constructor(
    public dialogRef: MatDialogRef<NeoTokenDialogHubComponent>,
    @Inject(MAT_DIALOG_DATA)
    public data: {
      isNew: boolean;
      neoToken: NeoToken;
      next: any;
      refresher: any;
    },
    public dialog: MatDialog,
  ) { }

  ngOnInit(): void {
    console.log(this.data);
  }

  openNewNeoTokenDialog() {
    this.dialog.open(NeoTokenDialogUpdatedComponent, {
      width: "850px",
      maxHeight: "90vh",
      data: {},
    });
  }

  openLegacyNeoTokenDialog() {
    this.dialog.open(NeoTokenDialogComponent, {
      width: "850px",
      data: this.data,
    });
  }

  close() {
    this.dialogRef.close();
  }
}
