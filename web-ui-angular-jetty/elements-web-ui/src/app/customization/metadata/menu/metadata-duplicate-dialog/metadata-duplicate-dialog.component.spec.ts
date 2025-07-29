import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MetadataDuplicateDialogComponent } from './metadata-duplicate-dialog.component';

describe('MetadataDuplicateDialogComponent', () => {
  let component: MetadataDuplicateDialogComponent;
  let fixture: ComponentFixture<MetadataDuplicateDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ MetadataDuplicateDialogComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MetadataDuplicateDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
