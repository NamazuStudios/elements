import {Component, Inject, OnInit} from '@angular/core';
import {FormBuilder, FormControl, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material/dialog';
import {MatChipInputEvent} from '@angular/material/chips';
import {MatSnackBar} from '@angular/material/snack-bar';
import {COMMA, ENTER} from '@angular/cdk/keycodes';
import {AlertService} from '../../../alert.service';
import {MetadataspecSelectDialogComponent} from "../../metadata-spec/metadataspec-select-dialog/metadataspec-select-dialog.component";
import {MetadataSpecProperty} from "../../../api/models/token-spec-tab";
import {APIError} from '../../../api/models/api-error';
import {Metadata} from "../../../api/models/metadata-tab";

export interface ItemCategoryPair {
  key: string;
  description: string;
}

@Component({
  selector: 'app-metadata-dialog',
  templateUrl: './metadata-dialog.component.html',
  styleUrls: ['./metadata-dialog.component.css']
})
export class MetadataDialogComponent implements OnInit {

  selectable = true;
  removable = true;
  addOnBlur = true;
  readonly separatorKeyCodes: number[] = [ENTER, COMMA];

  itemForm = this.formBuilder.group({
    name: [this.data.name, [Validators.required, Validators.pattern('^[_a-zA-Z0-9]+$')]],
    level: [this.data.level, [Validators.required]],
    metadataSpec: [{value: (this.data.spec) ? this.data.spec.name : null, disabled: true}],
  });

  metadataSpecForm = this.formBuilder.group({});
  formMap = [];
  private metadataFormNestLevel: number = 0;

  constructor(
    public dialogRef: MatDialogRef<MetadataDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    public dialog: MatDialog,
    private formBuilder: FormBuilder,
    private alertService: AlertService,
    private snackBar: MatSnackBar) {
  }

  close(saveChanges?: boolean): void {
    if (!saveChanges) {
      this.dialogRef.close();
      return;
    }

    const formData = this.itemForm.value;

    if (this.data.spec) {
      formData.metadataSpec = this.data.spec.id;
      formData.metadata = this.makeNestedObjectFromDotSeparated(this.metadataSpecForm.getRawValue());
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

      if (prop.type === 'NUMBER') {
        this.formMap.push({name: controlName, type: prop.type, nestLvl: this.metadataFormNestLevel, placeholder: prop.placeholder})
        this.metadataSpecForm.addControl(controlName, new FormControl(controlValue, [Validators.pattern('^[0-9]+$')]));
      }
      if (prop.type === 'OBJECT') {
        this.metadataFormNestLevel++;
        this.formMap.push({name: controlName, type: prop.type, nestLvl: this.metadataFormNestLevel})
        this.addFormControlsFromProperties(controlName, prop.properties,values);
      }
      if (prop.type === 'STRING' || prop.type === 'BOOLEAN') {
        this.formMap.push({name: controlName, type: prop.type, nestLvl: this.metadataFormNestLevel, placeholder: prop.placeholder})
        this.metadataSpecForm.addControl(controlName, new FormControl(controlValue));
      }
    });
    this.metadataFormNestLevel--;
  }

  getNestedMargin(nestLevel: number) {
    return 'ml-' + nestLevel;
  }

  getPlaceholder(item: any[]) {
    return (item['placeholder']) ? item['placeholder'] : item['name'];
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
