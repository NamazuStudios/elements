import { Component, Inject, OnInit } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';

@Component({
  selector: 'app-token-view-lightbox-dialog',
  templateUrl: './token-view-lightbox-dialog.component.html',
  styleUrls: ['./token-view-lightbox-dialog.component.css']
})
export class TokenViewLightboxDialogComponent implements OnInit {

  constructor(
    public dialogRef: MatDialogRef<TokenViewLightboxDialogComponent>,
    @Inject(MAT_DIALOG_DATA)
    public data: {
      mediaUrl: string
    }
  ) { }

  ngOnInit(): void {
  }

  getMediaType(url: string): string {
    const extension = url.split('.').pop();
    return ['png', 'jpg', 'svg', 'jpeg'].includes(extension) ? 'img' : 'video';
  }

  close() {
    this.dialogRef.close();
  }

}
