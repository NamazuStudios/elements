import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { GameOnApplicationConfigurationDialogComponent } from './game-on-application-configuration-dialog.component';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from "@angular/material/dialog";
import {FormBuilder, FormsModule, ReactiveFormsModule} from "@angular/forms";

describe('GameOnApplicationConfigurationDialogComponent', () => {
  let component: GameOnApplicationConfigurationDialogComponent;
  let fixture: ComponentFixture<GameOnApplicationConfigurationDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ GameOnApplicationConfigurationDialogComponent ],
      imports: [MatDialogModule, FormsModule, ReactiveFormsModule],
      providers: [
        FormBuilder,
        { provide: MatDialogRef, useValue: {} },
        { provide: MAT_DIALOG_DATA, useValue: { applicationConfiguration: {
              gameId: 1234,
              publicApiKey: "apiKey",
              adminApiKey: "adminKey",
              publicKey: "publicKey",
              parent: {
                id: 1234
              }
            }}}
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(GameOnApplicationConfigurationDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
