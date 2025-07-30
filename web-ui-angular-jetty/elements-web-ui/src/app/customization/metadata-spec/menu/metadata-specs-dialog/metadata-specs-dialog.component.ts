import {CdkDragDrop} from '@angular/cdk/drag-drop';
import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material/dialog';
import {MetadataSpec, MetadataSpecProperty, MetadataSpecPropertyType,} from '../../../../api/models/metadata-spec-tab';
import {MetadataSpecsService} from '../../../../api/services/metadata-specs.service';
import {
  MetadataSpecDialogDefineObjectComponent
} from "./metadata-spec-dialog-define-object/metadata-spec-dialog-define-object.component";

interface PropertyType {
  key: string;
  value: MetadataSpecPropertyType;
}

const complexFields = [
  MetadataSpecPropertyType.OBJECT,
  MetadataSpecPropertyType.ARRAY,
];

export const enumRegex = /^[a-zA-Z0-9_]+(,[a-zA-Z0-9_]+)*$/;

@Component({
  selector: 'metadata-specs-dialog',
  templateUrl: './metadata-specs-dialog.component.html',
  styleUrls: ['./metadata-specs-dialog.component.css']
})
export class MetadataSpecsDialogComponent implements OnInit {
  specName = '';
  properties: MetadataSpecProperty[] = [];
  propertiesTypes: PropertyType[];
  activeFieldIndex = 0;
  expandedField = null;
  // Workaround for accordion animation on init
  disableAnimation = true;
  showError: boolean = false;

  constructor(
    private metadataSpecsService: MetadataSpecsService,
    public dialogRef: MatDialogRef<MetadataSpecsDialogComponent>,
    public dialog: MatDialog,
    @Inject(MAT_DIALOG_DATA)
    public data: {
      template: MetadataSpec,
      refresh: Function,
    },
  ) {
  }

  ngOnInit(): void {
    this.propertiesTypes = Object.keys(MetadataSpecPropertyType)
      //TODO: implement
      .filter(key => key !== MetadataSpecPropertyType.ARRAY)
      .filter(key => key !== MetadataSpecPropertyType.TAGS)
      .map(key => ({
      key,
      value: MetadataSpecPropertyType[key]
    }));
    if (this.data.template) {
      const {name, type, properties} = this.data.template;
      this.specName = name;
      this.properties = properties;
    } else {
      this.properties = [this.createField()];
    }
  }

  createField(): MetadataSpecProperty {
    return {
      name: '',
      displayName: '',
      type: MetadataSpecPropertyType.STRING,
      required: false,
      placeholder: '',
      defaultValue: '',
      properties: []
    };
  }

  convertFieldsToArray(fields): MetadataSpecProperty[] {
    if (fields?.length !== undefined) return fields;
    const keys = Object.keys(fields);
    const newFields: MetadataSpecProperty[] = [];
    for (let i = 0; i < keys.length; i++) {
      const field = fields[keys[i]];
      newFields.push({
        name: field?.name || '',
        displayName: field?.displayName || '',
        type: field?.fieldType || MetadataSpecPropertyType.STRING,
        required: field.required || false,
        placeholder: field.placeholder,
        defaultValue: complexFields.includes(field.fieldType) ? JSON.parse(field.defaultValue) : field.defaultValue,
        properties: field.properties || []
      });
    }
    return newFields;
  }

  addNewField() {
    this.properties = [...this.properties, this.createField()];
  }

  handleFieldPanelStateChange(index: number) {
    this.expandedField = index;
  }

  changeFieldName(target: EventTarget, fieldIndex: number) {
    this.properties = this.properties.map((field, index) => {
      if (fieldIndex === index) {
        return {
          ...field,
          name: (target as HTMLInputElement).value,
        };
      }
      return field;
    });
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

  drop(event: CdkDragDrop<string[]>) {
    const fields = [...this.properties];
    const currentField = {...fields[event.previousIndex]};
    fields[event.previousIndex] = {...fields[event.currentIndex]};
    fields[event.currentIndex] = currentField;
    this.properties = fields;
  }

  removeField(fieldIndex: number) {
    this.properties = this.properties.filter(
      (_: MetadataSpecProperty, index: number): boolean => {
        return fieldIndex !== index;
      }
    );
  }

  duplicateProperty(fieldIndex: number) {
    this.properties = [...this.properties, this.properties[fieldIndex]];
  }


  changeTokenName(value: string) {
    this.specName = value;
  }

  openDefineObjectModal(index: number): void {
    this.dialog.open(MetadataSpecDialogDefineObjectComponent, {
      width: '800px',
      data: {
        updateProperties: this.updatePropertiesFromObjectComponent.bind(this),
        properties: this.properties[index].properties
      }
    });
    this.activeFieldIndex = index;
  }

  updatePropertiesFromObjectComponent(data: MetadataSpecProperty[]) {
    this.properties[this.activeFieldIndex].properties = data;
  }

  isValid(): boolean {
    return !!(this.specName) && this.properties.every(property => {
      return !!(property.name && property.type);
    });
  }

  close() {
    this.dialogRef.close();
  }

  async submit() {
    if (!this.isValid()) {
      this.showError = true;
      return;
    }
    this.showError = false;
    this.close();

    // Convert body for the api format
    const body = {
      name: this.specName,
      type: MetadataSpecPropertyType.OBJECT,
      properties: this.properties
    };

    if (this.data.template) {
      this.metadataSpecsService.updateMetadataSpec(this.data.template.id, body)
        .subscribe(() => {
          this.data.refresh();
        });
    } else {
      this.metadataSpecsService.createMetadataSpec(body)
        .subscribe(() => {
          this.data.refresh();
        });
    }
  }
}
