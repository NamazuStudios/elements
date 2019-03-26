import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MissionRewardsEditorComponent } from './mission-rewards-editor.component';

describe('MissionRewardsEditorComponent', () => {
  let component: MissionRewardsEditorComponent;
  let fixture: ComponentFixture<MissionRewardsEditorComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MissionRewardsEditorComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MissionRewardsEditorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
