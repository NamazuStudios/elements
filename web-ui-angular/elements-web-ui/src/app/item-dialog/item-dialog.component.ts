import {Component, Inject, OnInit} from '@angular/core';
import {FormBuilder, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatChipInputEvent, MatDialogRef} from '@angular/material';
import {ENTER, COMMA, TAB} from '@angular/cdk/keycodes';

@Component({
  selector: 'app-item-dialog',
  templateUrl: './item-dialog.component.html',
  styleUrls: ['./item-dialog.component.css']
})
export class ItemDialogComponent implements OnInit {
  selectable = true;
  removable = true;
  addOnBlur = true;
  readonly separatorKeyCodes: number[] = [ENTER, TAB, COMMA];

  constructor(public dialogRef: MatDialogRef<ItemDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any,
              private formBuilder: FormBuilder) { }

  itemForm = this.formBuilder.group({
    name: [ this.data.item.name, [Validators.required, Validators.pattern('^[a-zA-Z0-9]+$') ]],
    displayName: [ this.data.item.displayName, [Validators.required]],
    description: [ this.data.item.description ],
    tags: [ this.data.item.tags ]
  });

  addTag(event: MatChipInputEvent): void {
    const input = event.input;
    const value = event.value;

    if ((value || '').trim()) {
      this.data.item.tags.push(value);
    }

    if (input) {
      input.value = '';
    }
    console.log(this.data.item.tags);
  }

  ngOnInit() {
  }

}
