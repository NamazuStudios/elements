import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { LeaderboardDialogComponent } from './leaderboard-dialog.component';

describe('LeaderboardDialogComponent', () => {
  let component: LeaderboardDialogComponent;
  let fixture: ComponentFixture<LeaderboardDialogComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ LeaderboardDialogComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LeaderboardDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
