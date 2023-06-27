import {Component, Input, OnInit, ViewChild} from '@angular/core';
import {JsonEditorComponent, JsonEditorOptions} from 'ang-jsoneditor';

@Component({
  selector: 'app-json-editor-card',
  templateUrl: './json-editor-card.component.html',
  styleUrls: ['./json-editor-card.component.css']
})
export class JsonEditorCardComponent implements OnInit {
  @ViewChild(JsonEditorComponent) editor: JsonEditorComponent;

  @Input() editTarget: any;
  @Input() topic: string;

  showAdvanced = false;
  public isJSONValid = true;
  public editorOptions: JsonEditorOptions;

  constructor() {
    this.editorOptions = new JsonEditorOptions();
    this.initEditorOptions(this.editorOptions);
  }

  initEditorOptions(opts: JsonEditorOptions) {
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
    opts.onChange = () => this.validateMetadata(false);
  }

  toggleAdvancedEditor() {
    // update metadata JSON if leaving advanced editor
    if (this.showAdvanced) {
      this.validateMetadata(true);
    }
    this.showAdvanced = !this.showAdvanced;
  }

  validateMetadata(andUpdate: boolean) {
    try {
      // editor.get() throws error if JSON invalid
      const editorContents = this.editor.get();

      if (andUpdate) { this.editTarget.metadata = editorContents; }
      this.isJSONValid = true;
    } catch (err) {
      // bad JSON detected...don't let them leave the advanced editor!
      this.isJSONValid = false;
      return;
    }
  }

  ngOnInit() {
  }

}
