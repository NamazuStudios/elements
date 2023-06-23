import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { UserSelectDialogComponent } from './user-select-dialog.component';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {AlertService} from '../alert.service';
import {RouterTestingModule} from '@angular/router/testing';
import {MatSnackBar, MatSnackBarModule} from '@angular/material/snack-bar';
import {MatPaginator, MatPaginatorModule} from '@angular/material/paginator';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';

describe('UserSelectDialogComponent', () => {
  let component: UserSelectDialogComponent;
  let fixture: ComponentFixture<UserSelectDialogComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ UserSelectDialogComponent ],
      imports: [MatDialogModule, HttpClientTestingModule, RouterTestingModule, MatSnackBarModule, MatPaginatorModule, BrowserAnimationsModule],
      providers: [
        AlertService,
        MatSnackBar,
        MatPaginator,
        { provide: MatDialogRef, useValue: {} },
        { provide: MAT_DIALOG_DATA, useValue: {} }
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UserSelectDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
