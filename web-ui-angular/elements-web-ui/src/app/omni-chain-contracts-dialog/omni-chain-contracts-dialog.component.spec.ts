import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OmniChainContractsDialogComponent } from './omni-chain-contracts-dialog.component';

describe('OmniChainContractsDialogComponent', () => {
  let component: OmniChainContractsDialogComponent;
  let fixture: ComponentFixture<OmniChainContractsDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ OmniChainContractsDialogComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(OmniChainContractsDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
