import {ComponentFixture, TestBed, waitForAsync} from '@angular/core/testing';

import { ProfileDialogComponent } from './profile-dialog.component';
import {MAT_DIALOG_DATA, MatDialog, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {FormBuilder, FormsModule, ReactiveFormsModule} from '@angular/forms';
import {AlertService} from '../../alert.service';
import {RouterTestingModule} from '@angular/router/testing';
import {MatSnackBar, MatSnackBarModule} from '@angular/material/snack-bar';
import {MatSelect, MatSelectModule} from '@angular/material/select';
import {MatInputModule} from '@angular/material/input';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';

describe('ProfileDialogComponent', () => {
  let component: ProfileDialogComponent;
  let fixture: ComponentFixture<ProfileDialogComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ ProfileDialogComponent ],
      imports: [MatDialogModule, HttpClientTestingModule, FormsModule, ReactiveFormsModule,
        RouterTestingModule, MatSnackBarModule, MatSelectModule, MatInputModule, BrowserAnimationsModule],
      providers: [
        FormBuilder,
        AlertService,
        MatSnackBar,
        MatSelect,
        {provide: MatDialogRef, useValue: {} },
        { provide: MAT_DIALOG_DATA, useValue: { profile: {
              displayName: 'display name',
              imageUrl: 'image.url',
              application: 'application',
              user: {
                name: 'username',
                email: 'user@test.com',
                facebookId: 1234
              },
              metadata: {
                meta: 12,
                data: 34
              }
            }}},
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProfileDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
