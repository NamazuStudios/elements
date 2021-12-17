import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { InventoryEditorComponent } from './inventory-editor.component';

describe('InventoryEditorComponent', () => {
  let component: InventoryEditorComponent;
  let fixture: ComponentFixture<InventoryEditorComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ InventoryEditorComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(InventoryEditorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
