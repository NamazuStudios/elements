import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { JsonEditorCardComponent } from './json-editor-card.component';

describe('JsonEditorCardComponent', () => {
  let component: JsonEditorCardComponent;
  let fixture: ComponentFixture<JsonEditorCardComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ JsonEditorCardComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(JsonEditorCardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
