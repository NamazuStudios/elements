import { Component, Inject, OnInit } from '@angular/core';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MetadataSpecProperty, MetadataSpecPropertyType } from '../../../../../api/models/metadata-spec-tab';

@Component({
  selector: 'metadata-spec-dialog-define-object',
  templateUrl: './metadata-spec-dialog-define-object.component.html',
  styleUrls: ['./metadata-spec-dialog-define-object.component.css']
})
export class MetadataSpecDialogDefineObjectComponent implements OnInit {

  properties = [];
  propertiesTypes = [];
  activeObjectIndex = null;
  expandedField = null;
  // Workaround for accordion animation on init
  disableAnimation = true;

  constructor(
    public dialogRef: MatDialogRef<MetadataSpecDialogDefineObjectComponent>,
    public dialog: MatDialog,
    @Inject(MAT_DIALOG_DATA)
    public parentData: {
      updateProperties: Function,
      properties: MetadataSpecProperty[]
    },
  ) { }

  ngOnInit(): void {
    if (this.parentData.properties) {
      this.properties = this.parentData.properties;
    } else {
      this.properties = [this.createField()];
    }
    this.propertiesTypes = Object.keys(MetadataSpecPropertyType)
      //TODO: implement
      .filter(key => key !== MetadataSpecPropertyType.ARRAY)
      .filter(key => key !== MetadataSpecPropertyType.TAGS)
      .map(key => ({key, value: MetadataSpecPropertyType[key]}));
  }

  handleFieldPanelStateChange(index: number) {
    this.expandedField = index;
  }

  createField(): MetadataSpecProperty {
    return {
      name: '',
      displayName: '',
      required: false,
      type: MetadataSpecPropertyType.STRING,
      defaultValue: '',
      placeholder: '',
      properties: []
    };
  }

  removeField(fieldIndex: number): void {
    this.properties = this.properties.filter((_: MetadataSpecProperty, index: number): boolean => {
      return index !== fieldIndex;
    });
  }

  addNewField(): void {
    this.disableAnimation = true;
    this.properties = [...this.properties, this.createField()];
    setTimeout(() => this.disableAnimation = false, 10);
  }

  duplicateField(field: MetadataSpecProperty): void {
    if (field) {
      this.properties = [...this.properties, field];
    }
  }

  changeFieldName(target: EventTarget, fieldIndex: number) {
    this.properties = this.properties.map((field, index) => {
      if (fieldIndex === index && target) {
        return {
          ...field,
          name: (target as HTMLInputElement).value,
        }
      }
      return field;
    });
  }

  updateFieldType(type: MetadataSpecPropertyType, index: number): void {
    this.disableAnimation = true;
    this.properties = this.properties.map((field: MetadataSpecProperty, i: number): MetadataSpecProperty => {
      if (index === i) {
        return {
          ...field,
          type: MetadataSpecPropertyType[type],
        }
      }
      return field;
    });
    setTimeout(() => this.disableAnimation = false);
  }

  openDefineObjectModal(index: number): void {
    this.dialog.open(MetadataSpecDialogDefineObjectComponent, {
      width: '800px',
      data: {
        updateProperties: this.updatePropertiesFromObjectComponent.bind(this),
        properties: this.properties[index].properties
      }
    });
    this.activeObjectIndex = index;
  }

  updatePropertiesFromObjectComponent(data: MetadataSpecProperty[]) {
    this.properties[this.activeObjectIndex].properties = data;
  }

  changePropertyType(typeKey: MetadataSpecPropertyType, fieldIndex): void {
    this.disableAnimation = true;
    this.properties = this.properties.map(
      (field: MetadataSpecProperty, index: number): MetadataSpecProperty => {
        if (index === fieldIndex) {
          return {
            name: '',
            displayName: '',
            required: false,
            defaultValue: '',
            placeholder: '',
            type: MetadataSpecPropertyType[typeKey],
            properties: []
          };
        }
        return field;
      }
    );
    setTimeout(() => this.disableAnimation = false);
  }

  changeDefaultValue(value: string, propertyIndex: string | number): void {
    this.properties[propertyIndex].defaultValue = value;
  }
  changePlaceholder(value: string, propertyIndex: string | number): void {
    this.properties[propertyIndex].placeholder = value;
  }

  close(): void {
    this.dialogRef.close();
  }

  updateProperties(fields: MetadataSpecProperty[]): void {
    this.properties = fields.map((field: MetadataSpecProperty, index: number): MetadataSpecProperty => {
      if (index === this.activeObjectIndex) {
        return {
          ...field,
        }
      }
      return field;
    });
    console.log('field ',this.properties);
  }

  submit(): void {
    this.parentData.updateProperties(this.properties);
    this.dialogRef.close();
  }
}
