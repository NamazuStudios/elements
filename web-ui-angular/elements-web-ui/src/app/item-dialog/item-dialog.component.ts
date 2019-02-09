import {Component, Inject, OnInit} from '@angular/core';
import {FormBuilder, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material';

@Component({
  selector: 'app-item-dialog',
  templateUrl: './item-dialog.component.html',
  styleUrls: ['./item-dialog.component.css']
})
export class ItemDialogComponent implements OnInit {

  constructor(public dialogRef: MatDialogRef<ItemDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any,
              private formBuilder: FormBuilder) { }

  itemForm = this.formBuilder.group({
    name: [ this.data.item.name, [Validators.required, Validators.pattern('^[a-zA-Z0-9]+$') ]],
    displayName: [ this.data.item.displayName, [Validators.required]],
    description: [ this.data.item.description ],
  });

  ngOnInit() {
  }

}
