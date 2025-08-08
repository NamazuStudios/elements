import {Component, Inject, OnInit} from '@angular/core';
import {FormBuilder, FormControl, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material/dialog';
import {MatChipInputEvent} from '@angular/material/chips';
import {MatSnackBar} from '@angular/material/snack-bar';
import {COMMA, ENTER} from '@angular/cdk/keycodes';
import {AlertService} from '../../alert.service';
import {ItemCategory} from "../../api/models/item";
import {MetadataspecSelectDialogComponent} from "../../customization/metadata-spec/metadataspec-select-dialog/metadataspec-select-dialog.component";
import {MetadataSpecProperty} from "../../api/models/token-spec-tab";
import {APIError} from '../../api/models/api-error';

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
    {key: ItemCategory.FUNGIBLE, description: "Fungible"},
    {key: ItemCategory.DISTINCT, description: "Distinct"}
  ];

  selectable = true;
  removable = true;
  addOnBlur = true;
  readonly separatorKeyCodes: number[] = [ENTER, COMMA];

  itemForm = this.formBuilder.group({
    name: [this.data.item.name, [Validators.required, Validators.pattern('^[_a-zA-Z0-9]+$')]],
    displayName: [this.data.item.displayName, [Validators.required]],
    description: [this.data.item.description, [Validators.required]],
    category: [this.data.item.category, [Validators.required]],
    metadataSpec: [{value: (this.data.item.metadataSpec) ? this.data.item.metadataSpec.name : null, disabled: true}],
    publicVisible: [this.data.item.publicVisible],
    tags: []
  });

  metadataSpecForm = this.formBuilder.group({});
  formMap = [];
  private metadataFormNestLevel: number = 0;

  constructor(public dialogRef: MatDialogRef<ItemDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any, public dialog: MatDialog,
              private formBuilder: FormBuilder, private alertService: AlertService, private snackBar: MatSnackBar) {
  }

  addTag(event: MatChipInputEvent): void {
    const input = event.input;
    const value = event.value;

    if ((value || '').trim()) {
      if (!this.data.item.tags) {
        this.data.item.tags = [];
      }
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
      this.dialogRef.close();
      return;
    }

    const formData = this.itemForm.value;
    if (this.data.item.tags !== undefined) {
      formData.tags = this.data.item.tags;
    }

    if (this.data.item.metadataSpec) {
      formData.metadataSpec = this.data.item.metadataSpec;
      formData.metadata = this.makeNestedObjectFromDotSeparated(this.metadataSpecForm.getRawValue());
    }

    if (!this.data.isNew && formData.category !== undefined) {
      delete formData.category
    }

    this.data.next(formData).subscribe(() => {
      this.dialogRef.close();
      this.data.refresher.refresh();
    }, (err: APIError) => {
      this.alertService.error(err);
    });
  }

  ngOnInit() {
    this.alertService.getMessage().subscribe((message: any) => {
      if (message) {
        this.snackBar.open(message.text, 'Dismiss', { duration: 3000 });
      }
    });
    if (this.data.item.metadata && this.data.item.metadataSpec) {
      this.initMetadataSpecForm(this.data.item.metadata);
    }
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
          this.initMetadataSpecForm(null);
        }
      }
    });
  }

  private initMetadataSpecForm(values: any[]) {
    this.metadataFormNestLevel = 0;
    this.formMap = [];
    let props : MetadataSpecProperty[] = this.data.item.metadataSpec.properties;
    this.addFormControlsFromProperties("", props, values);
  }

  private addFormControlsFromProperties(currentName: string, props: MetadataSpecProperty[], values: any[]) {

    props.forEach(prop => {

      let controlName = ((currentName) && currentName.length > 0) ? (currentName + "." + prop.name) : prop.name;
      let controlValue = (values) ? this.getValueByPath(values, controlName) : '';

      switch (prop.type) {

        case 'NUMBER':
          this.formMap.push({name: controlName, type: prop.type, nestLvl: this.metadataFormNestLevel, placeholder: prop.placeholder})
          this.metadataSpecForm.addControl(controlName, new FormControl(controlValue, [Validators.pattern('^[0-9]+$')]));
          break;
        case 'STRING':
          this.formMap.push({name: controlName, type: prop.type, nestLvl: this.metadataFormNestLevel, placeholder: prop.placeholder})
          this.metadataSpecForm.addControl(controlName, new FormControl(controlValue));
          break;
        case 'OBJECT':
          this.metadataFormNestLevel++;
          this.formMap.push({name: controlName, type: prop.type, nestLvl: this.metadataFormNestLevel})
          this.addFormControlsFromProperties(controlName, prop.properties,values);
          break;
        case 'BOOLEAN':
          this.formMap.push({name: controlName, type: prop.type, nestLvl: this.metadataFormNestLevel, placeholder: prop.placeholder})
          this.metadataSpecForm.addControl(controlName, new FormControl(controlValue));
          break;
      }
    });

    this.metadataFormNestLevel--;
  }

  getNestedMargin(nestLevel: number) {
    return 'ml-' + nestLevel;
  }

  getPlaceholder(item: any[]) {
    return item['placeholder'];
  }

  private makeNestedObjectFromDotSeparated(obj: {}) {
    const result = {};
    for (const key in obj) {
      key.split('.').reduce((res, k, i, arr) => {
        return res[k] = arr.length - 1 === i
          ? obj[key]
          : res[k] || {};
      }, result);
    }
    return result;
  };

  private getValueByPath(obj: any, path: string): any {
    let parts = path.split('.');
    let value = obj;
    for (let part of parts) {
      if (value && part in value) {
        value = value[part];
      } else {
        return undefined;
      }
    }
    return value;
  }
}
