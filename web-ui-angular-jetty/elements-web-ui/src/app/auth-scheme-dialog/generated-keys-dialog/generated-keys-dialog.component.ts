import { Component, Inject } from "@angular/core";
import {
  MatDialog,
  MatDialogRef,
  MAT_DIALOG_DATA,
} from "@angular/material/dialog";
import { AlertService } from "src/app/alert.service";
import { Clipboard } from "@angular/cdk/clipboard";
import { MatSnackBar } from "@angular/material/snack-bar";

@Component({
  selector: 'app-generated-keys-dialog',
  templateUrl: './generated-keys-dialog.component.html',
  styleUrls: ['./generated-keys-dialog.component.css']
})
export class GeneratedKeysDialogComponent {

  constructor(
    public dialogRef: MatDialogRef<GeneratedKeysDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private alertService: AlertService,
    private clipboard: Clipboard,
    public dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  copyKeyToClipboard(data: string) {
    this.clipboard.copy(data);
    this.snackBar.open("Key has been copied to clipboard.", "Dismiss", {
      duration: 3000,
    });
  }

  close() {
    this.dialogRef.close();
  }

}
