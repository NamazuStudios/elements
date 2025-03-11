import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { IosApplicationConfigurationDialogComponent } from './ios-application-configuration-dialog.component';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from "@angular/material/dialog";
import {FormBuilder, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {AlertService} from "../../alert.service";
import {RouterTestingModule} from "@angular/router/testing";
import {MatSnackBar} from "@angular/material/snack-bar";

describe('IosApplicationConfigurationDialogComponent', () => {
  let component: IosApplicationConfigurationDialogComponent;
  let fixture: ComponentFixture<IosApplicationConfigurationDialogComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ IosApplicationConfigurationDialogComponent ],
      imports: [MatDialogModule, FormsModule, ReactiveFormsModule, RouterTestingModule],
      providers: [
        FormBuilder,
        AlertService,
        MatSnackBar,
        { provide: MatDialogRef, useValue: {} },
        { provide: MAT_DIALOG_DATA, useValue: { applicationConfiguration: {
              applicationId: 1234,
              category: "category",
              publicKey: "publicKey",
              appleSignInConfiguration: {
                teamId: 1234,
                clientId: 1234,
                keyId: 1234,
                appleSignInPrivateKey: "prvKey"
              }
            }}}
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(IosApplicationConfigurationDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
