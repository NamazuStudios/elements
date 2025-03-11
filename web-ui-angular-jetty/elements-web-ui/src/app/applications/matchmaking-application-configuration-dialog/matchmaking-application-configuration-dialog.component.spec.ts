import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { MatchmakingApplicationConfigurationDialogComponent } from './matchmaking-application-configuration-dialog.component';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from "@angular/material/dialog";
import {FormBuilder, FormsModule, ReactiveFormsModule} from "@angular/forms";

describe('MatchmakingApplicationConfigurationDialogComponent', () => {
  let component: MatchmakingApplicationConfigurationDialogComponent;
  let fixture: ComponentFixture<MatchmakingApplicationConfigurationDialogComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ MatchmakingApplicationConfigurationDialogComponent ],
      imports: [MatDialogModule, FormsModule, ReactiveFormsModule],
      providers: [
        FormBuilder,
        { provide: MatDialogRef, useValue: {} },
        { provide: MAT_DIALOG_DATA, useValue: { applicationConfiguration: {
              scheme: 1234,
              success: {
                module: "module",
                method: "method"
              },
              parent: {
                id: 1234
              }
            }}},
      ]

    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MatchmakingApplicationConfigurationDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
