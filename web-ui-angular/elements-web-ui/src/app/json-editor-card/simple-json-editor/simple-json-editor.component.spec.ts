import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SimpleJsonEditorComponent } from './simple-json-editor.component';

describe('SimpleJsonEditorComponent', () => {
  let component: SimpleJsonEditorComponent;
  let fixture: ComponentFixture<SimpleJsonEditorComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SimpleJsonEditorComponent ]
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
