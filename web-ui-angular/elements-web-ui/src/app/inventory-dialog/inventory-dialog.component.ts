import { Component, Inject, OnInit, ViewChild } from '@angular/core';
import {FormBuilder, Validators} from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { User } from '../api/models';
import { JsonEditorCardComponent } from '../json-editor-card/json-editor-card.component';

@Component({
  selector: 'app-inventory-dialog',
  templateUrl: './inventory-dialog.component.html',
  styleUrls: ['./inventory-dialog.component.css']
})
export class InventoryDialogComponent implements OnInit {

  dataHolder: any = {metadata: null};

  userId: string;

  profileId: string;

  profileForm = this.formBuilder.group({
    userId: [{value: this.data.user.id, disabled: true}],
    userName: [{value: this.data.user.name, disabled: true}],
  });

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: any,
    private formBuilder: FormBuilder,
    public dialogRef: MatDialogRef<InventoryDialogComponent>
  ) { }

  ngOnInit(): void {
    this.userId = this.data.user.id;
    this.profileId = this.data.profile?.id;
  }

  close(saveChanges?: boolean): void {
    // just close and don't do anything.....
    if (!saveChanges) {
      //this.data.profile.metadata = this.originalMetadata;
      this.dialogRef.close();
      return;
    }
  }

}
