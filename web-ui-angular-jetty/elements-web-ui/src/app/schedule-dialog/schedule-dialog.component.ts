import {Component, Inject, OnInit} from '@angular/core';
import {FormBuilder, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar'
import {AlertService} from '../alert.service';

@Component({
  selector: 'app-item-dialog',
  templateUrl: './schedule-dialog.component.html',
  styleUrls: ['./schedule-dialog.component.css']
})
export class ScheduleDialogComponent implements OnInit {

  constructor(public dialogRef: MatDialogRef<ScheduleDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any,
              private formBuilder: FormBuilder, private alertService: AlertService, private snackBar: MatSnackBar) { }

  okButtonEnabled = true;
  selectable = true;
  removable = true;
  addOnBlur = true;

  scheduleForm = this.formBuilder.group({
    name: [ this.data.schedule.name, [Validators.required, Validators.pattern('^[_a-zA-Z0-9]+$') ]],
    displayName: [ this.data.schedule.displayName, [Validators.required]],
    description: [ this.data.schedule.description ]
  });

  close(saveChanges?: boolean): void {
    if (!saveChanges) {
      this.dialogRef.close();
      return;
    }
    const formData = this.scheduleForm.value;

    this.data.next(formData).subscribe(() => {
      this.dialogRef.close();
      this.data.refresher.refresh();
    }, err => {
      this.alertService.error(err);
    });
  }

  ngOnInit() {
    this.alertService.getMessage().subscribe((message: any) => {
      if(message) {
        this.snackBar.open(message.text, "Dismiss", { duration: 3000 });
      }
    });
  }

}
