import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NeoSmartContractMintDialogComponent } from './neo-smart-contract-mint-dialog.component';

describe('NeoSmartContractMintDialogComponent', () => {
  let component: NeoSmartContractMintDialogComponent;
  let fixture: ComponentFixture<NeoSmartContractMintDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ NeoSmartContractMintDialogComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(NeoSmartContractMintDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
