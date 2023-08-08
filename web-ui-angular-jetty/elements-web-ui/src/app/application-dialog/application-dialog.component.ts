import {Component, Inject, OnInit, ViewChild} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';
import {FormBuilder, Validators} from '@angular/forms';
import {AlertService} from '../alert.service';

@Component({
  selector: 'app-application-dialog',
  templateUrl: './application-dialog.component.html',
  styleUrls: ['./application-dialog.component.css']
})
export class ApplicationDialogComponent implements OnInit {

  showAdvanced = false;
  public isJSONValid = true;

  applicationForm = this.formBuilder.group({
    name: [ this.data.application.name, [Validators.required, Validators.pattern('^[a-zA-Z0-9]+$') ]],
    description: [ this.data.application.description ],
    scriptRepoUrl: [ { value: this.data.application.scriptRepoUrl, disabled: true } ]
  });

  toggleAdvancedEditor() {
    if (this.showAdvanced) {
      this.validateMetadata(true);
    }
    this.showAdvanced = !this.showAdvanced;
  }

  constructor(public dialogRef: MatDialogRef<ApplicationDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any,
              private formBuilder: FormBuilder, private alertService: AlertService, private snackBar: MatSnackBar) { }

  ngOnInit() {
    this.alertService.getMessage().subscribe((message: any) => {
      if(message) {
        this.snackBar.open(message.text, 'Dismiss', { duration: 3000 });
      }
    });
  }

  close(res?: any) {
    if (!res) {
      this.dialogRef.close();
      return;
    }

    this.data.next(res).subscribe(r => {
      this.dialogRef.close();
      this.data.refresher.refresh();
    }, err => {
      this.alertService.error(err);
    });
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
