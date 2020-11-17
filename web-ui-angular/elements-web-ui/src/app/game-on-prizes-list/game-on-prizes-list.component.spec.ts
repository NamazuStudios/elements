import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { GameOnPrizesListComponent } from './game-on-prizes-list.component';

describe('GameOnApplicationPrizesListComponent', () => {
  let component: GameOnPrizesListComponent;
  let fixture: ComponentFixture<GameOnPrizesListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ GameOnPrizesListComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(GameOnPrizesListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
