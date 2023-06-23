import { Component, Inject, OnInit } from '@angular/core';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { NeoSmartContract } from '../api/models/blockchain/neo-smart-contract';
import { NeoToken } from '../api/models/blockchain/neo-token';
import { NeoSmartContractsService } from '../api/services/blockchain/neo-smart-contracts.service';
import { TokenViewLightboxDialogComponent } from '../token-view-lightbox-dialog/token-view-lightbox-dialog.component';

@Component({
  selector: 'app-token-viewer-dialog',
  templateUrl: './token-viewer-dialog.component.html',
  styleUrls: ['./token-viewer-dialog.component.css']
})
export class TokenViewerDialogComponent implements OnInit {
  currentSmartContract: NeoSmartContract;

  constructor(
    public dialogRef: MatDialogRef<TokenViewerDialogComponent>,
    @Inject(MAT_DIALOG_DATA)
    public data: {
      token: NeoToken
    },
    private neoSmartContractService: NeoSmartContractsService,
    public dialog: MatDialog
  ) { }

  ngOnInit(): void {
    this.neoSmartContractService
      .getNeoSmartContract(this.data.token.contractId)
      .subscribe((neoContract) => {
        this.currentSmartContract = JSON.parse(JSON.stringify(neoContract));
      });
  }

  isMedia(url: string): boolean {
    const extension = url.split('.').pop();
    return ['png', 'jpg', 'svg', 'jpeg', 'mp4', 'vid', 'ogv'].includes(extension);
  }

  close() {
    this.dialogRef.close();
  }

  showLightboxDialog(url) {
    this.dialog.open(TokenViewLightboxDialogComponent, {
      width: "850px",
      data: {
        mediaUrl: url
      }
    })
  }

}
