import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MetadataSpecsDuplicateDialogComponent } from './metadata-specs-duplicate-dialog.component';

describe('MetadataSpecsDuplicateDialogComponent', () => {
  let component: MetadataSpecsDuplicateDialogComponent;
  let fixture: ComponentFixture<MetadataSpecsDuplicateDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ MetadataSpecsDuplicateDialogComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MetadataSpecsDuplicateDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
