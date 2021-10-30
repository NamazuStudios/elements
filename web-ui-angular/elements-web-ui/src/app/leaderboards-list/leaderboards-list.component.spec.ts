import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { LeaderboardsListComponent } from './leaderboards-list.component';

describe('LeaderboardsListComponent', () => {
  let component: LeaderboardsListComponent;
  let fixture: ComponentFixture<LeaderboardsListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ LeaderboardsListComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LeaderboardsListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
