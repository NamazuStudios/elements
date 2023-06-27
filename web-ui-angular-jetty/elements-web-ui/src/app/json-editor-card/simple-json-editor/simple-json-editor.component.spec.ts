import { async, ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { SimpleJsonEditorComponent } from './simple-json-editor.component';
import {FormBuilder, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatFormField, MatFormFieldControl, MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';

describe('SimpleJsonEditorComponent', () => {
  let component: SimpleJsonEditorComponent;
  let fixture: ComponentFixture<SimpleJsonEditorComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ SimpleJsonEditorComponent ],
      imports: [MatFormFieldModule, FormsModule, ReactiveFormsModule, MatInputModule, BrowserAnimationsModule],
      providers: [
        FormBuilder,
        MatFormField,
        MatFormFieldControl
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SimpleJsonEditorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
