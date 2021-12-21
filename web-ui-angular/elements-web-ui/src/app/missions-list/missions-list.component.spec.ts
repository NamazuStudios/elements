import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { MissionsListComponent } from './missions-list.component';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {MissionsService} from '../api/services/missions.service';
import {AlertService} from '../alert.service';
import {RouterTestingModule} from '@angular/router/testing';
import {ConfirmationDialogService} from '../confirmation-dialog/confirmation-dialog.service';
import {MatDialogModule} from '@angular/material/dialog';
import {MatPaginator, MatPaginatorModule} from '@angular/material/paginator';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';

describe('MissionsListComponent', () => {
  let component: MissionsListComponent;
  let fixture: ComponentFixture<MissionsListComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ MissionsListComponent ],
      imports: [HttpClientTestingModule, RouterTestingModule, MatDialogModule, MatPaginatorModule, BrowserAnimationsModule],
      providers: [
        MissionsService,
        AlertService,
        ConfirmationDialogService,
        MatPaginator
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MissionsListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
