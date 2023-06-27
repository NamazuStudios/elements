import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NeoSmartTokenSpecsMoveFieldDialogComponent } from './neo-smart-token-specs-move-field-dialog.component';

describe('NeoSmartTokenSpecsMoveFieldDialogComponent', () => {
  let component: NeoSmartTokenSpecsMoveFieldDialogComponent;
  let fixture: ComponentFixture<NeoSmartTokenSpecsMoveFieldDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ NeoSmartTokenSpecsMoveFieldDialogComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(NeoSmartTokenSpecsMoveFieldDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
