import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MissionStepsCardComponent } from './mission-steps-card.component';
import {FormBuilder, FormsModule, ReactiveFormsModule} from '@angular/forms';

describe('MissionStepsCardComponent', () => {
  let component: MissionStepsCardComponent;
  let fixture: ComponentFixture<MissionStepsCardComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MissionStepsCardComponent ],
      imports: [FormsModule, ReactiveFormsModule],
      providers: [
        FormBuilder
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MissionStepsCardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
