import {Component, ElementRef, Input, ViewChild} from '@angular/core';
import {Application} from '../api/models/application';
import {JsonEditorComponent, JsonEditorOptions} from 'ang-jsoneditor';
import {FormBuilder} from '@angular/forms';

@Component({
  selector: 'app-application-attributes',
  templateUrl: './application-attributes.component.html',
  styleUrls: ['./application-attributes.component.css']
})
export class ApplicationAttributesComponent {
  @ViewChild(JsonEditorComponent) editor: JsonEditorComponent;
  @ViewChild(`newKey`) newKeyRef: ElementRef;
  @ViewChild(`attributeScroll`) attributeScrollRef: ElementRef;

  @Input() application: Application;

  showAdvanced = false;
  public isJSONValid = true;
  public editorOptions: JsonEditorOptions = this.getJsonEditorOptions();

  constructor(private formBuilder: FormBuilder) { }

  attributesForm = this.formBuilder.group({
    newKey: [],
    newValue: []
  });

  toggleAdvancedEditor() {
    if (this.showAdvanced) {
      this.validateAttributes(true);
    }
    this.showAdvanced = !this.showAdvanced;
  }

  getJsonEditorOptions(): JsonEditorOptions  {
    const opts = new JsonEditorOptions();
    opts.modes = ['code', 'text', 'view'];
    opts.mode = 'code';
    opts.schema = {
      'properties': {
        'testProp': {
          'type': 'integer'
        }
      },
      'additionalProperties': {
        'type': ['string', 'number']
      }
    };
    opts.onChange = () => this.validateAttributes(false);
    return opts;
  }

  validateAttributes(andUpdate: boolean) {
    try {
      if (andUpdate) { this.application.attributes = this.editor.get(); }
      this.isJSONValid = true;
    } catch (err) {
      this.isJSONValid = false;
      return;
    }
  }

  editAttribute(originalKey, newKey, newValue) {
    if (newKey === originalKey) {
      this.application.attributes[originalKey] = newValue;
      return;
    }

    delete this.application.attributes[originalKey];

    this.addAttribute(newKey, newValue, true);
  }

  addAttribute(key, value, isEdit = false) {
    if (!key || !value) { return; }

    if (!this.application.attributes) {
      this.application.attributes = {};
    }

    // attempt to extract a number; on failure, keep as string
    const attemptNum: number = Number(value);
    if (!isNaN(attemptNum)) {
      this.application.attributes[key] = attemptNum;
    } else {
      this.application.attributes[key] = value;
    }

    if(!isEdit) {
      this.attributesForm.reset();
      this.newKeyRef.nativeElement.focus();

      // async'd to delay scrolling until after metadata element added to UI
      setTimeout(() => this.attributeScrollRef.nativeElement.scrollIntoView(false));
    }
  }

  removeDataAtKey(key) {
    delete this.application.attributes[key];
  }
}
