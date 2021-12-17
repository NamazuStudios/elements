import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { FacebookApplicationConfigurationDialogComponent } from './facebook-application-configuration-dialog.component';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from "@angular/material/dialog";
import {FormBuilder, FormsModule, ReactiveFormsModule} from "@angular/forms";

describe('FacebookApplicationConfigurationsDialogComponent', () => {
  let component: FacebookApplicationConfigurationDialogComponent;
  let fixture: ComponentFixture<FacebookApplicationConfigurationDialogComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ FacebookApplicationConfigurationDialogComponent ],
      imports: [MatDialogModule, FormsModule, ReactiveFormsModule],
      providers: [
        FormBuilder,
        { provide: MatDialogRef, useValue: {} },
        { provide: MAT_DIALOG_DATA, useValue: { applicationConfiguration: {
              applicationId: 1234,
              applicationSecret: "secret",
              parent: {
                  id: 1234
                }
            }}}
        ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(FacebookApplicationConfigurationDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
