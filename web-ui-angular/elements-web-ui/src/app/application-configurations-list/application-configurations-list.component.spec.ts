import {async, ComponentFixture, TestBed, waitForAsync} from '@angular/core/testing';

import { ApplicationConfigurationsListComponent } from './application-configurations-list.component';
import {HttpClient, HttpHandler} from "@angular/common/http";
import {AlertService} from "../alert.service";
import {ConfirmationDialogService} from "../confirmation-dialog/confirmation-dialog.service";
import {MatDialog, MatDialogModule} from "@angular/material/dialog";
import {MatMenuModule} from "@angular/material/menu";
import {MatPaginator, MatPaginatorModule} from "@angular/material/paginator";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";

describe('ApplicationConfigurationsListComponent', () => {
  let component: ApplicationConfigurationsListComponent;
  let fixture: ComponentFixture<ApplicationConfigurationsListComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ ApplicationConfigurationsListComponent ],
      imports: [MatDialogModule, MatMenuModule, MatPaginatorModule, BrowserAnimationsModule],
      providers: [
        HttpClient,
        HttpHandler,
        {provide: MatPaginator, useValue: MatPaginator},
        {provide: MatDialog, useValue: MatDialog},
        {provide: ConfirmationDialogService, useValue: ConfirmationDialogService},
        {provide: AlertService, useValue: AlertService}
        ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ApplicationConfigurationsListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
