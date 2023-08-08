import {Component, Input, ViewChild} from '@angular/core';
import {Application} from '../api/models/application';
import {JsonEditorComponent, JsonEditorOptions} from 'ang-jsoneditor';

@Component({
  selector: 'app-application-attributes',
  templateUrl: './application-attributes.component.html',
  styleUrls: ['./application-attributes.component.css']
})
export class ApplicationAttributesComponent {
  @ViewChild(JsonEditorComponent) editor: JsonEditorComponent;
  @Input() application: Application;

  showAdvanced = false;
  public isJSONValid = true;
  public editorOptions: JsonEditorOptions = this.getJsonEditorOptions();

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
    opts.onChange = () => this.validateAttributes(true);
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
}
