import {ComponentFixture, TestBed, waitForAsync} from '@angular/core/testing';

import { ProfilesListComponent } from './profiles-list.component';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {AlertService} from '../../alert.service';
import {RouterTestingModule} from '@angular/router/testing';
import {ConfirmationDialogService} from '../../confirmation-dialog/confirmation-dialog.service';
import {MatDialog, MatDialogModule} from '@angular/material/dialog';
import {MatPaginator, MatPaginatorModule} from '@angular/material/paginator';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';

describe('ProfilesListComponent', () => {
  let component: ProfilesListComponent;
  let fixture: ComponentFixture<ProfilesListComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ ProfilesListComponent ],
      imports: [HttpClientTestingModule, RouterTestingModule, MatDialogModule, MatPaginatorModule, BrowserAnimationsModule],
      providers: [
        AlertService,
        ConfirmationDialogService,
        MatPaginator,
        MatDialog
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProfilesListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
