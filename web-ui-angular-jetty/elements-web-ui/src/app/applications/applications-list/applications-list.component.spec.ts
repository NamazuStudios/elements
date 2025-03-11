import { async, ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ApplicationsListComponent } from './applications-list.component';
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {ApplicationsService} from "../../api/services/applications.service";
import {AlertService} from "../../alert.service";
import {RouterTestingModule} from "@angular/router/testing";
import {ConfirmationDialogService} from "../../confirmation-dialog/confirmation-dialog.service";
import {MatDialogModule} from "@angular/material/dialog";
import {MatPaginator, MatPaginatorModule} from "@angular/material/paginator";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";

describe('ApplicationsListComponent', () => {
  let component: ApplicationsListComponent;
  let fixture: ComponentFixture<ApplicationsListComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ ApplicationsListComponent ],
      imports: [HttpClientTestingModule, RouterTestingModule, MatDialogModule, MatPaginatorModule, BrowserAnimationsModule],
      providers: [
        ApplicationsService,
        AlertService,
        ConfirmationDialogService,
        {provide: MatPaginator, useValue: MatPaginator},
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ApplicationsListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
