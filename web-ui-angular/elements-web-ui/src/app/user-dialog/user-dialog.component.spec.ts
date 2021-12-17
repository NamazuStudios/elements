import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { UserDialogComponent } from './user-dialog.component';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {FormBuilder, FormsModule, ReactiveFormsModule} from '@angular/forms';
import {AlertService} from '../alert.service';
import {RouterTestingModule} from '@angular/router/testing';
import {MatSnackBar, MatSnackBarModule} from '@angular/material/snack-bar';
import {MatSelectModule} from '@angular/material/select';
import {MatInputModule} from '@angular/material/input';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';

describe('UserDialogComponent', () => {
  let component: UserDialogComponent;
  let fixture: ComponentFixture<UserDialogComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ UserDialogComponent ],
      imports: [MatDialogModule, FormsModule, ReactiveFormsModule, RouterTestingModule,
        MatSnackBarModule, MatSelectModule, MatInputModule, BrowserAnimationsModule],
      providers: [
        FormBuilder,
        AlertService,
        MatSnackBar,
        { provide: MatDialogRef, useValue: {} },
        { provide: MAT_DIALOG_DATA, useValue: { user: {
              id: 1234,
              name: 'new user',
              email: 'newuser@test.com',
              level: 1
            }}},
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UserDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
