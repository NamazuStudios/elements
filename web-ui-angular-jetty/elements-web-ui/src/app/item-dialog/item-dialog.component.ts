import {Component, Inject, OnInit, ViewChild} from '@angular/core';
import {FormArray, FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material/dialog';
import {MatChipInputEvent} from '@angular/material/chips';
import {MatSnackBar} from '@angular/material/snack-bar';
import {COMMA, ENTER} from '@angular/cdk/keycodes';
import {JsonEditorCardComponent} from '../json-editor-card/json-editor-card.component';
import {AlertService} from '../alert.service';
import {ItemCategory} from "../api/models/item";
import {MetadataspecSelectDialogComponent} from "../metadataspec-select-dialog/metadataspec-select-dialog.component";
import {MetadataSpecProperty, MetadataSpecPropertyType} from "../api/models/token-spec-tab";

export interface ItemCategoryPair {
  key: string;
  description: string;
}

@Component({
  selector: 'app-item-dialog',
  templateUrl: './item-dialog.component.html',
  styleUrls: ['./item-dialog.component.css']
})
export class ItemDialogComponent implements OnInit {

  itemCategories: ItemCategoryPair[] = [
    { key: ItemCategory.FUNGIBLE, description: "Fungible" },
    { key: ItemCategory.DISTINCT, description: "Distinct" }
  ];

  @ViewChild(JsonEditorCardComponent) editorCard: JsonEditorCardComponent;

  selectable = true;
  removable = true;
  addOnBlur = true;
  readonly separatorKeyCodes: number[] = [ENTER, COMMA];

  originalMetadata = JSON.parse(JSON.stringify(this.data.item.metadata || {}));

  itemForm = this.formBuilder.group({
    name: [ this.data.item.name, [Validators.required, Validators.pattern('^[_a-zA-Z0-9]+$') ]],
    displayName: [ this.data.item.displayName, [Validators.required]],
    description: [ this.data.item.description, [Validators.required]],
    category: [ this.data.item.category, [Validators.required] ],
    metadataSpec: [ { value: this.data.item.metadataSpec, disabled: true } ],
    publicVisible: [ this.data.item.publicVisible],
    tags: []
  });

  metadataSpecForm = this.formBuilder.group({});
  formMap = [];
  private metadataFormNestLevel: number = 0;

  constructor(public dialogRef: MatDialogRef<ItemDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any, public dialog: MatDialog,
              private formBuilder: FormBuilder, private alertService: AlertService, private snackBar: MatSnackBar) {}

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

    if (!this.data.isNew && formData.category !== undefined) {
      delete formData.category
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
      if (message) {
        this.snackBar.open(message.text, 'Dismiss', { duration: 3000 });
      }
    });
  }

  currentItemCategory() {
    return this.data.item.category == null
      ? this.itemCategories[0].description
      : this.itemCategories.find(value => this.data.item.category == value.key).description
  }

  showSelectMetadataSpecDialog() {
    this.dialog.open(MetadataspecSelectDialogComponent, {
      width: '700px',
      data: {
        next: result => {
          this.data.item.metadataSpec = result;
          Object.keys(this.metadataSpecForm.controls).forEach(key => {
            this.metadataSpecForm.removeControl(key);
          });
          this.metadataFormNestLevel = 0;
          this.formMap = [];
          let props : MetadataSpecProperty[] = this.data.item.metadataSpec.properties;
          this.addFormControlsFromProperties("", props);
          console.log('created map: ', this.formMap);
        }
      }
    });
  }

  private addFormControlsFromProperties(currentName: string, props: MetadataSpecProperty[]) {
    console.log("adding props: ", props);
    props.forEach(prop => {
      let controlName = ((currentName) && currentName.length > 0) ? (currentName + "." + prop.name) : prop.name;
      if (prop.type === 'NUMBER') {
        this.formMap.push({name: prop.name, type: prop.type, nestLvl: this.metadataFormNestLevel})
        this.metadataSpecForm.addControl(prop.name, new FormControl('', [Validators.pattern('^[0-9]+$')]));
      }
      if (prop.type === 'OBJECT') {
        this.metadataFormNestLevel++;
        this.formMap.push({name: prop.name, type: prop.type, nestLvl: this.metadataFormNestLevel})
        this.addFormControlsFromProperties(controlName, prop.properties);
      }
      if (prop.type === 'STRING' || prop.type === 'BOOLEAN') {
        this.formMap.push({name: prop.name, type: prop.type, nestLvl: this.metadataFormNestLevel})
        this.metadataSpecForm.addControl(prop.name, new FormControl(('')));
      }
    });
    this.metadataFormNestLevel--;
  }

  getNestedMargin(nestLevel: number) {
    return 'ml-' + nestLevel;
  }
}
