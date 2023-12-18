import {CdkDragDrop} from '@angular/cdk/drag-drop';
import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material/dialog';
import {MetadataSpec, MetadataSpecProperty, MetadataSpecPropertyType,} from '../api/models/token-spec-tab';
import {MetadataSpecsService} from '../api/services/metadata-specs.service';
import {
  NeoSmartTokenSpecsMoveFieldDialogComponent
} from '../neo-smart-token-specs-move-field-dialog/neo-smart-token-specs-move-field-dialog.component';
import {
  NeoTokenDialogDefineObjectComponent
} from '../neo-token-dialog-define-object/neo-token-dialog-define-object.component';

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
  selector: 'app-neo-smart-token-specs-dialog',
  templateUrl: './neo-smart-token-specs-dialog.component.html',
  styleUrls: ['./neo-smart-token-specs-dialog.component.css']
})
export class NeoSmartTokenSpecsDialogComponent implements OnInit {
  specName = '';
  properties: MetadataSpecProperty[] = [];
  propertiesTypes: PropertyType[];
  activeFieldIndex = 0;
  expandedField = null;
  // Workaround for accordion animation on init
  disableAnimation = true;

  constructor(
    private metadataSpecsService: MetadataSpecsService,
    public dialogRef: MatDialogRef<NeoSmartTokenSpecsDialogComponent>,
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
      .filter(key => key !== MetadataSpecPropertyType.ARRAY && key !== MetadataSpecPropertyType.OBJECT)
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

  ngAfterViewInit(): void {
    // timeout required to avoid the dreaded 'ExpressionChangedAfterItHasBeenCheckedError'
    setTimeout(() => this.disableAnimation = false);

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

  // convertFieldsToObject(properties: MetadataSpecProperty[]) {
  //   if (properties?.length === undefined) return properties;
  //   const newProperties = {};
  //   for (let i = 0; i < properties.length; i++) {
  //     const field = properties[i];
  //     let defaultValue;
  //     if (field.defaultValue) {
  //       if (complexFields.includes(field.type)) {
  //         defaultValue = JSON.stringify(field.defaultValue);
  //       } else {
  //         defaultValue = field.defaultValue;
  //       }
  //     }
  //     newProperties[i] = {
  //       name: field.name,
  //       displayName: field?.displayName,
  //       type: field.type,
  //       required: field.required,
  //       placeHolder: field.placeHolder,
  //       defaultValue: field.defaultValue,
  //       properties: this.convertFieldsToObject(field.properties)
  //     }
  //   };
  //   return newProperties;
  // }

  addNewField() {
    this.properties = [...this.properties, this.createField()];
  }

  handleFieldPanelStateChange(index: number) {
    this.expandedField = index;
  }

  changeFieldName(value: string, fieldIndex: number) {
    this.properties = this.properties.map((field, index) => {
      if (fieldIndex === index) {
        return {
          ...field,
          name: value,
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

  changeDefaultValue(value, propertyIndex): void {
      this.properties[propertyIndex].defaultValue = value;
  }
  changePlaceholder(value, propertyIndex): void {
    this.properties[propertyIndex].placeholder = value;
  }

  drop(event: CdkDragDrop<string[]>) {
    const fields = [...this.properties];
    const currentField = {...fields[event.previousIndex]};
    const fieldToReplace = {...fields[event.currentIndex]};
    fields[event.previousIndex] = fieldToReplace;
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
    this.dialog.open(NeoTokenDialogDefineObjectComponent, {
      width: '800px',
      data: {
        // updateFieldsWithContent: this.updateFieldsWithContent.bind(this),
        content: this.properties[index]?.defaultValue, // TODO was conent before, not sure what now
      }
    });
    this.activeFieldIndex = index;
  }

  changeFieldTab(fieldIndex: number, newTabIndex: number) {
    let field;
    const newFields = this.properties?.filter(
      (f: MetadataSpecProperty, index: number): boolean => {
        if (index === fieldIndex) {
          field = f;
        }
        return index !== fieldIndex;
      }
    );
    this.properties = newFields;
  }

  movePropertyToAnotherParent(index: number) {
    this.dialog.open(NeoSmartTokenSpecsMoveFieldDialogComponent, {
      width: '500px',
      data: {
        fieldIndex: index,
        activeTabIndex: this.activeFieldIndex + 1,
        max: this.properties.length,
        changeFieldTab: this.changeFieldTab.bind(this),
      }
    });
    this.activeFieldIndex = index;
  }

  // Tab actions

  //

  // updateFieldsWithContent(data) {
  //   const fieldIndex = data.index || data.index === 0 ? data.index : this.activeFieldIndex;
  //   this.properties = this.properties.map(
  //     (field: MetadataSpecProperty, index: number) => {
  //       if (index === fieldIndex) {
  //         const content = data.hasOwnProperty('content') ? data.content : data;
  //         return {
  //           ...field,
  //           content: content && !content?.otherProps ? content : field.content,
  //           placeHolder: data?.otherProps?.placeHolder || field.placeHolder || '',
  //           defaultValue: data?.otherProps?.defaultValue || field.defaultValue || '',
  //         }
  //       }
  //       return field;
  //     }
  //   );
  // }

  isValid(): boolean {
    let isValid = true;
    if (!this.specName) {
      isValid = false;
    }

    for (let i = 0; i < this.properties.length; i++) {
      const field = this.properties[i];
      if (field?.type === MetadataSpecPropertyType.ENUM && !enumRegex.test(field.defaultValue as string)) {
        isValid = false;
      }
    }
    return isValid;
  }

  close() {
    this.dialogRef.close();
  }

  async submit() {
    this.close();

    // Convert body for the api format
    const body = {
      name: this.specName,
      type: MetadataSpecPropertyType.OBJECT,
      properties: this.properties
    };

    if (this.data.template) {
      this.metadataSpecsService.updateTokenTemplate(this.data.template.id, body)
        .subscribe(() => {
          this.data.refresh();
        });
    } else {
      this.metadataSpecsService.createTokenSpec(body)
        .subscribe(() => {
          this.data.refresh();
        });
    }
  }

  private parseBoolean(stringVal: string): boolean {
    if (stringVal === 'true') {
      return true;
    } else {
      return false;
    }
  }
}
