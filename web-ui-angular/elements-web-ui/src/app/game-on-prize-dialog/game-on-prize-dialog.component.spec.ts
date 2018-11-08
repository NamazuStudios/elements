import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { GameOnPrizeDialogComponent } from './game-on-prize-dialog.component';

describe('GameOnPrizeDialogComponent', () => {
  let component: GameOnPrizeDialogComponent;
  let fixture: ComponentFixture<GameOnPrizeDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ GameOnPrizeDialogComponent ]
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
