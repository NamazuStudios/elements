import {Component, Inject, OnInit, ViewChild} from '@angular/core';
import {FormBuilder, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatChipInputEvent, MatDialogRef} from '@angular/material';
import {ENTER, COMMA} from '@angular/cdk/keycodes';
import {JsonEditorOptions, JsonEditorComponent} from 'ang-jsoneditor';
import {JsonEditorCardComponent} from '../json-editor-card/json-editor-card.component';
import {Mission} from '../api/models/mission';

@Component({
  selector: 'app-item-dialog',
  templateUrl: './mission-dialog.component.html',
  styleUrls: ['./mission-dialog.component.css']
})
export class MissionDialogComponent implements OnInit {

  constructor(public dialogRef: MatDialogRef<MissionDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any,
              private formBuilder: FormBuilder) { }

  @ViewChild(JsonEditorCardComponent) editorCard: JsonEditorCardComponent;

  selectable = true;
  removable = true;
  addOnBlur = true;
  readonly separatorKeyCodes: number[] = [ENTER, COMMA];

  missionForm = this.formBuilder.group({
    name: [ this.data.mission.name, [Validators.required, Validators.pattern('^[a-zA-Z0-9]+$') ]],
    displayName: [ this.data.mission.displayName, [Validators.required]],
    description: [ this.data.mission.description ],
    tags: []
  });

  addTag(event: MatChipInputEvent): void {
    const input = event.input;
    const value = event.value;

    if ((value || '').trim()) {
      if (this.data.mission.tags == undefined) { this.data.mission.tags = []; }
      this.data.mission.tags.push(value);
    }

    if (input) {
      input.value = '';
    }
  }

  remove(tag: string): void {
    const index = this.data.mission.tags.indexOf(tag);

    if (index >= 0) {
      this.data.mission.tags.splice(index, 1);
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

    this.editorCard.validateMetadata(true);

    const formData = this.missionForm.value;
    if (this.data.mission.tags !== undefined) {
      formData.tags = this.data.mission.tags;
    }
    if (this.data.mission.metadata !== undefined) {
      formData.metadata = this.data.mission.metadata;
    }
    console.log(formData);
    this.dialogRef.close(formData);
  }

  ngOnInit() {
  }

}
