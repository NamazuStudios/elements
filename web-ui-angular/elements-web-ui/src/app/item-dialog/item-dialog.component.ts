import {Component, Inject, OnInit, ViewChild} from '@angular/core';
import {FormBuilder, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatChipInputEvent, MatDialogRef} from '@angular/material';
import {ENTER, COMMA} from '@angular/cdk/keycodes';
import {JsonEditorOptions, JsonEditorComponent} from 'ang-jsoneditor';

@Component({
  selector: 'app-item-dialog',
  templateUrl: './item-dialog.component.html',
  styleUrls: ['./item-dialog.component.css']
})
export class ItemDialogComponent implements OnInit {

  constructor(public dialogRef: MatDialogRef<ItemDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any,
              private formBuilder: FormBuilder) {
    this.editorOptions = new JsonEditorOptions();
    this.initEditorOptions(this.editorOptions);
  }
  @ViewChild(JsonEditorComponent) editor: JsonEditorComponent;

  isJSONValid = true;
  selectable = true;
  removable = true;
  addOnBlur = true;
  showAdvanced = false;
  public editorOptions: JsonEditorOptions;
  readonly separatorKeyCodes: number[] = [ENTER, COMMA];

  itemForm = this.formBuilder.group({
    name: [ this.data.item.name, [Validators.required, Validators.pattern('^[a-zA-Z0-9]+$') ]],
    displayName: [ this.data.item.displayName, [Validators.required]],
    description: [ this.data.item.description ],
    tags: []
  });

  initEditorOptions(opts: JsonEditorOptions) {
    opts.modes = ['code', 'text', 'view'];
    opts.mode = 'code';
    opts.onChange = () => this.validateMetadata(false);
  }

  toggleAdvancedEditor() {
    // update metadata JSON if leaving advanced editor
    if(this.showAdvanced) {
      this.validateMetadata(true);
    }
    this.showAdvanced = !this.showAdvanced;
  }

  validateMetadata(andUpdate: boolean) {
    try {
      const editorContents = this.editor.get();
      if (andUpdate) { this.data.item.metadata = editorContents; }
      this.isJSONValid = true;
    } catch (err) {
      // bad JSON detected...don't let them leave the advanced editor!
      this.isJSONValid = false;
      return;
    }
  }

  addTag(event: MatChipInputEvent): void {
    const input = event.input;
    const value = event.value;

    if ((value || '').trim()) {
      if (this.data.item.tags == undefined) { this.data.item.tags = []; }
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
  close(saveChanges: boolean): void {
    if (!saveChanges) {
      this.dialogRef.close();
      return;
    }

    this.validateMetadata(true);

    const formData = this.itemForm.value;
    if (this.data.item.tags !== undefined) {
      formData.tags = this.data.item.tags;
    }
    if (this.data.item.metadata !== undefined) {
      formData.metadata = this.data.item.metadata;
    }
    this.dialogRef.close(formData);
  }

  ngOnInit() {
  }

}
