import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NeoSmartTokenSpecsDialogComponent } from './neo-smart-token-specs-dialog.component';

describe('NeoSmartTokenSpecsDialogComponent', () => {
  let component: NeoSmartTokenSpecsDialogComponent;
  let fixture: ComponentFixture<NeoSmartTokenSpecsDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ NeoSmartTokenSpecsDialogComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(NeoSmartTokenSpecsDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
