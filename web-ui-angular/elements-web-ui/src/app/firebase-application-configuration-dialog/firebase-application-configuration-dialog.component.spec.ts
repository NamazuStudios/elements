import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { FirebaseApplicationConfigurationDialogComponent } from './firebase-application-configuration-dialog.component';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from "@angular/material/dialog";
import {FormBuilder, FormsModule, ReactiveFormsModule} from "@angular/forms";

describe('FirebaseApplicationConfigurationDialogComponent', () => {
  let component: FirebaseApplicationConfigurationDialogComponent;
  let fixture: ComponentFixture<FirebaseApplicationConfigurationDialogComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ FirebaseApplicationConfigurationDialogComponent ],
      imports: [MatDialogModule, FormsModule, ReactiveFormsModule],
      providers: [
        FormBuilder,
        { provide: MatDialogRef, useValue: {} },
        { provide: MAT_DIALOG_DATA, useValue: { applicationConfiguration: {
              projectId: 1234,
              serviceAccountCredentials: "creds",
              parent: {
                id: 1234
              }
            }}}
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(FirebaseApplicationConfigurationDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
