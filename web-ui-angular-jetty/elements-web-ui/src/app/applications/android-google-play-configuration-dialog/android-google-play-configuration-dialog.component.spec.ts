import {ComponentFixture, TestBed, waitForAsync} from '@angular/core/testing';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from "@angular/material/dialog";
import {FormBuilder, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import {AlertService} from '../../alert.service';
import {MatSnackBar} from '@angular/material/snack-bar';

import { AndroidGooglePlayConfigurationDialogComponent } from './android-google-play-configuration-dialog.component';

describe('AndroidGooglePlayConfigurationDialogComponent', () => {
  let component: AndroidGooglePlayConfigurationDialogComponent;
  let fixture: ComponentFixture<AndroidGooglePlayConfigurationDialogComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ AndroidGooglePlayConfigurationDialogComponent ],
      imports: [MatDialogModule, FormsModule, ReactiveFormsModule],
      providers: [
        FormBuilder,
        { provide: MatDialogRef, useValue: {} },
        { provide: MAT_DIALOG_DATA, useValue: {
          applicationConfiguration: {
              applicationId: "com.example.myapp",
              jsonKey: 'jsonKey'
            }}},
        { provide: AlertService, useValue: AlertService },
        { provide: MatSnackBar, useValue: {} }
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AndroidGooglePlayConfigurationDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
