import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { MissionDialogComponent } from './mission-dialog.component';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {FormBuilder, FormsModule, ReactiveFormsModule} from '@angular/forms';
import {AlertService} from '../../alert.service';
import {RouterTestingModule} from '@angular/router/testing';
import {MatSnackBar, MatSnackBarModule} from '@angular/material/snack-bar';

describe('MissionDialogComponent', () => {
  let component: MissionDialogComponent;
  let fixture: ComponentFixture<MissionDialogComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ MissionDialogComponent ],
      imports: [MatDialogModule, FormsModule, ReactiveFormsModule, RouterTestingModule, MatSnackBarModule],
      providers: [
        FormBuilder,
        AlertService,
        MatSnackBar,
        { provide: MatDialogRef, useValue: {} },
        { provide: MAT_DIALOG_DATA, useValue: { mission: {
              name: 'mission name',
              displayName: 'mission display name',
              description: 'mission description'
            }}},
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MissionDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
