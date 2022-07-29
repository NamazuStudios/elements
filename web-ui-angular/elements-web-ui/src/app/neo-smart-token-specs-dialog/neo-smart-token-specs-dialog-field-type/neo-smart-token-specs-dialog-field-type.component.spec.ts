import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NeoSmartTokenSpecsDialogFieldTypeComponent } from './neo-smart-token-specs-dialog-field-type.component';

describe('NeoSmartTokenSpecsDialogFieldTypeComponent', () => {
  let component: NeoSmartTokenSpecsDialogFieldTypeComponent;
  let fixture: ComponentFixture<NeoSmartTokenSpecsDialogFieldTypeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ NeoSmartTokenSpecsDialogFieldTypeComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(NeoSmartTokenSpecsDialogFieldTypeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
