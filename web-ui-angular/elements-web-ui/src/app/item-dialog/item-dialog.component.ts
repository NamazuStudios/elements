import {Component, Inject, OnInit, ViewChild} from '@angular/core';
import {FormBuilder, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatChipInputEvent, MatDialogRef, MatSnackBar} from '@angular/material';
import {ENTER, COMMA} from '@angular/cdk/keycodes';
import {JsonEditorOptions, JsonEditorComponent} from 'ang-jsoneditor';
import {JsonEditorCardComponent} from '../json-editor-card/json-editor-card.component';
import {AlertService} from '../alert.service';

@Component({
  selector: 'app-item-dialog',
  templateUrl: './item-dialog.component.html',
  styleUrls: ['./item-dialog.component.css']
})
export class ItemDialogComponent implements OnInit {

  @ViewChild(JsonEditorCardComponent) editorCard: JsonEditorCardComponent;

  selectable = true;
  removable = true;
  addOnBlur = true;
  readonly separatorKeyCodes: number[] = [ENTER, COMMA];

  originalMetadata = JSON.parse(JSON.stringify(this.data.item.metadata));

  itemForm = this.formBuilder.group({
    name: [ this.data.item.name, [Validators.required, Validators.pattern('^[a-zA-Z0-9]+$') ]],
    displayName: [ this.data.item.displayName, [Validators.required]],
    description: [ this.data.item.description, [Validators.required] ],
    tags: []
  });

  constructor(public dialogRef: MatDialogRef<ItemDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any,
              private formBuilder: FormBuilder, private alertService: AlertService, private snackBar: MatSnackBar) {
    console.log(this.originalMetadata);
  }

  addTag(event: MatChipInputEvent): void {
    const input = event.input;
    const value = event.value;

    if ((value || '').trim()) {
      if (!this.data.item.tags) { this.data.item.tags = []; }
      this.data.item.tags.push(value);
    }

    if (input) {
      input.value = '';
    }
  }

  remove(tag: string): void {
    const index = this.data.item.tags.indexOf(tag);

    if (index >= 0) {
      this.data.item.tags.splice(index, 1);
    }
  }

  /*
  * Can't just call dialogRef.close(itemForm.value) since it doesn't accurately
  * capture changes to item tags so we need to explicitly attach the entire tag
  * array to the itemForm, overwriting the initial tags value
  */
  close(saveChanges?: boolean): void {
    if (!saveChanges) {
      this.data.item.metadata = this.originalMetadata;
      this.dialogRef.close();
      return;
    }

    this.editorCard.validateMetadata(true);

    const formData = this.itemForm.value;
    if (this.data.item.tags !== undefined) {
      formData.tags = this.data.item.tags;
    }
    if (this.data.item.metadata !== undefined) {
      formData.metadata = this.data.item.metadata;
    }

    this.data.next(formData).subscribe(r => {
      this.dialogRef.close();
      this.data.refresher.refresh();
    }, err => {
      this.alertService.error(err);
    });
  }

  ngOnInit() {
    this.alertService.getMessage().subscribe((message: any) => {
      if(message) {
        this.snackBar.open(message.text, "Dismiss", { duration: 3000 });
      }
    });
  }

}
