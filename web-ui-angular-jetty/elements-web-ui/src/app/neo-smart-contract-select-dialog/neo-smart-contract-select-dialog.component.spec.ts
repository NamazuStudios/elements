import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NeoSmartContractSelectDialogComponent } from './neo-smart-contract-select-dialog.component';

describe('NeoSmartContractSelectDialogComponent', () => {
  let component: NeoSmartContractSelectDialogComponent;
  let fixture: ComponentFixture<NeoSmartContractSelectDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ NeoSmartContractSelectDialogComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(NeoSmartContractSelectDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
