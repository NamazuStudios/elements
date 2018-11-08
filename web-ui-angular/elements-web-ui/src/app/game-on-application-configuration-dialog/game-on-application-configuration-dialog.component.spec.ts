import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { GameOnApplicationConfigurationDialogComponent } from './game-on-application-configuration-dialog.component';

describe('GameOnApplicationConfigurationDialogComponent', () => {
  let component: GameOnApplicationConfigurationDialogComponent;
  let fixture: ComponentFixture<GameOnApplicationConfigurationDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ GameOnApplicationConfigurationDialogComponent ]
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
