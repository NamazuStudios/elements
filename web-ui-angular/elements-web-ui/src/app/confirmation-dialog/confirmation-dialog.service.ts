import { Injectable } from '@angular/core';
import { ConfirmationDialogComponent } from './confirmation-dialog.component';
import { MatDialogRef, MatDialog } from '@angular/material';
import {Observable} from "rxjs";

@Injectable()
export class ConfirmationDialogService {

  constructor(private dialog: MatDialog) { }

  public confirm(title: string, message: string): Observable<boolean> {

    let dialogRef: MatDialogRef<ConfirmationDialogComponent>;

    dialogRef = this.dialog.open(ConfirmationDialogComponent);

    dialogRef.componentInstance.title = title;
    dialogRef.componentInstance.message = message;

    return dialogRef.afterClosed();
  }
}
