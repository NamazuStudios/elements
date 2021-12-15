import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { UsersListComponent } from './users-list.component';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {AlertService} from '../alert.service';
import {RouterTestingModule} from '@angular/router/testing';
import {ConfirmationDialogService} from '../confirmation-dialog/confirmation-dialog.service';
import {MatDialog, MatDialogModule} from '@angular/material/dialog';
import {MatPaginator, MatPaginatorModule} from '@angular/material/paginator';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';

describe('UsersListComponent', () => {
  let component: UsersListComponent;
  let fixture: ComponentFixture<UsersListComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ UsersListComponent ],
      imports: [HttpClientTestingModule, RouterTestingModule, MatDialogModule, MatPaginatorModule, BrowserAnimationsModule],
      providers: [
        AlertService,
        ConfirmationDialogService,
        MatDialog,
        MatPaginator
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UsersListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
