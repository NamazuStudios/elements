import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MetadataSpecsDialogFieldTypeComponent } from './metadata-specs-dialog-field-type.component';

describe('MetadataSpecsDialogFieldTypeComponent', () => {
  let component: MetadataSpecsDialogFieldTypeComponent;
  let fixture: ComponentFixture<MetadataSpecsDialogFieldTypeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ MetadataSpecsDialogFieldTypeComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MetadataSpecsDialogFieldTypeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
