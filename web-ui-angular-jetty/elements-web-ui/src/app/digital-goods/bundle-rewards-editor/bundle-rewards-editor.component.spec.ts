import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { BundleRewardsEditorComponent } from './bundle-rewards-editor.component';
import {FormBuilder, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {HttpClientTestingModule} from "@angular/common/http/testing";

describe('BundleRewardsEditorComponent', () => {
  let component: BundleRewardsEditorComponent;
  let fixture: ComponentFixture<BundleRewardsEditorComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ BundleRewardsEditorComponent ],
      imports: [FormsModule, ReactiveFormsModule, HttpClientTestingModule],
      providers: [FormBuilder]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BundleRewardsEditorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
