import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ApplicationDialogComponent } from './application-dialog.component';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from "@angular/material/dialog";
import {FormBuilder, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {AlertService} from "../alert.service";
import {MatSnackBar} from "@angular/material/snack-bar";
import {RouterTestingModule} from "@angular/router/testing";

describe('ApplicationDialogComponent', () => {
  let component: ApplicationDialogComponent;
  let fixture: ComponentFixture<ApplicationDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ApplicationDialogComponent ],
      imports: [MatDialogModule, FormsModule, ReactiveFormsModule, RouterTestingModule],
      providers: [
        FormBuilder,
        { provide: MatDialogRef, useValue: {} },
        AlertService,
        { provide: MatSnackBar, useValue: {} },
        { provide: MAT_DIALOG_DATA, useValue: { application: {
              name: "new app",
              description: "This is a new app",
              scriptRepoUrl: "localhost"
            }}},
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ApplicationDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
