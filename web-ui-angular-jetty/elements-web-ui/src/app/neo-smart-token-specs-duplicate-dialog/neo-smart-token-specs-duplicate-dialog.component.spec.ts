import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NeoSmartTokenSpecsDuplicateDialogComponent } from './neo-smart-token-specs-duplicate-dialog.component';

describe('NeoSmartTokenSpecsDuplicateDialogComponent', () => {
  let component: NeoSmartTokenSpecsDuplicateDialogComponent;
  let fixture: ComponentFixture<NeoSmartTokenSpecsDuplicateDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ NeoSmartTokenSpecsDuplicateDialogComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(NeoSmartTokenSpecsDuplicateDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
