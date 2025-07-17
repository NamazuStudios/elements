import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MetadataSpecsDialogComponent } from './metadata-specs-dialog.component';

describe('NeoSmartTokenSpecsDialogComponent', () => {
  let component: MetadataSpecsDialogComponent;
  let fixture: ComponentFixture<MetadataSpecsDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ MetadataSpecsDialogComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MetadataSpecsDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
