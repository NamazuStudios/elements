import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { GameOnApplicationPrizesListComponent } from './game-on-prizes-list.component';

describe('GameOnApplicationPrizesListComponent', () => {
  let component: GameOnApplicationPrizesListComponent;
  let fixture: ComponentFixture<GameOnApplicationPrizesListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ GameOnApplicationPrizesListComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(GameOnApplicationPrizesListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
