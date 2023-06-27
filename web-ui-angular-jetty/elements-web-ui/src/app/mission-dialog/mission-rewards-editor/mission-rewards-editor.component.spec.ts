import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { MissionRewardsEditorComponent } from './mission-rewards-editor.component';
import {FormBuilder, FormsModule, ReactiveFormsModule} from '@angular/forms';
import {HttpClientTestingModule} from '@angular/common/http/testing';

describe('MissionRewardsEditorComponent', () => {
  let component: MissionRewardsEditorComponent;
  let fixture: ComponentFixture<MissionRewardsEditorComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ MissionRewardsEditorComponent ],
      imports: [FormsModule, ReactiveFormsModule, HttpClientTestingModule],
      providers: [
        FormBuilder
      ]
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
