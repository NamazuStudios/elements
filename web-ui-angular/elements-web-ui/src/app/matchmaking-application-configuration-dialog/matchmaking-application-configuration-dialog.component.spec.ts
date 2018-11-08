import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MatchmakingApplicationConfigurationDialogComponent } from './matchmaking-application-configuration-dialog.component';

describe('MatchmakingApplicationConfigurationDialogComponent', () => {
  let component: MatchmakingApplicationConfigurationDialogComponent;
  let fixture: ComponentFixture<MatchmakingApplicationConfigurationDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MatchmakingApplicationConfigurationDialogComponent ]
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
