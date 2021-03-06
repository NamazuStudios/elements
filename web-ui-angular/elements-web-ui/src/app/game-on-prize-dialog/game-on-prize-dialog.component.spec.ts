import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { GameOnPrizeDialogComponent } from './game-on-prize-dialog.component';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from "@angular/material/dialog";
import {FormBuilder, FormsModule, ReactiveFormsModule} from "@angular/forms";

describe('GameOnPrizeDialogComponent', () => {
  let component: GameOnPrizeDialogComponent;
  let fixture: ComponentFixture<GameOnPrizeDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ GameOnPrizeDialogComponent ],
      imports: [MatDialogModule, FormsModule, ReactiveFormsModule],
      providers: [
        FormBuilder,
        { provide: MatDialogRef, useValue: {} },
        { provide: MAT_DIALOG_DATA, useValue: { gameOnPrize: {
              title: "title",
              description: "this is a description",
              prizeInfo: "some prize info",
              imageUrl: "image.url"
            }}}
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(GameOnPrizeDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
