import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { MissionStepsCardComponent } from './mission-steps-card.component';
import {FormBuilder, FormsModule, ReactiveFormsModule} from '@angular/forms';

describe('MissionStepsCardComponent', () => {
  let component: MissionStepsCardComponent;
  let fixture: ComponentFixture<MissionStepsCardComponent>;

  beforeEach(waitForAsync(() => {
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
