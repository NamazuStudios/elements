import { Component, Inject } from "@angular/core";
import {
  MatDialog,
  MatDialogRef,
  MAT_DIALOG_DATA,
} from "@angular/material/dialog";
import { AlertService } from "src/app/alert.service";

@Component({
  selector: "app-regenerate-keys-dialog",
  templateUrl: "./regenerate-keys-dialog.component.html",
  styleUrls: ["./regenerate-keys-dialog.component.css"],
})
export class RegenerateKeysDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<RegenerateKeysDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private alertService: AlertService,
    public dialog: MatDialog
  ) {}

  close(saveChanges?: boolean) {
    this.data.next(saveChanges);
    this.dialogRef.close();
  }
}
