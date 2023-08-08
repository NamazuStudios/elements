import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {FormBuilder} from '@angular/forms';
import {AlertService} from '../alert.service';

@Component({
  selector: 'app-application-attributes',
  templateUrl: './application-attributes.component.html',
  styleUrls: ['./application-attributes.component.css']
})
export class ApplicationAttributesComponent implements OnInit {

  showAdvanced = false;
  public isJSONValid = true;

  toggleAdvancedEditor() {
    if (this.showAdvanced) {
      this.validateMetadata(true);
    }
    this.showAdvanced = !this.showAdvanced;
  }

  constructor(@Inject(MAT_DIALOG_DATA) public data: any,
              private formBuilder: FormBuilder, private alertService: AlertService, private snackBar: MatSnackBar) { }

  ngOnInit() {
  }

  // TODO: try unify with json-editor-card
  validateMetadata(andUpdate: boolean) {
    try {
      // editor.get() throws error if JSON invalid
      // const editorContents = this.editor.get();

      // if (andUpdate) { this.editTarget.metadata = editorContents; }
      this.isJSONValid = true;
    } catch (err) {
      // bad JSON detected...don't let them leave the advanced editor!
      this.isJSONValid = false;
      return;
    }
  }
}
