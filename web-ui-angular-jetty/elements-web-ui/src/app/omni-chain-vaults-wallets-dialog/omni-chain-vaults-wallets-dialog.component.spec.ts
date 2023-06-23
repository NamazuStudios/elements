import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OmniChainVaultsWalletsDialogComponent } from './omni-chain-vaults-wallets-dialog.component';

describe('OmniChainVaultsWalletsDialogComponent', () => {
  let component: OmniChainVaultsWalletsDialogComponent;
  let fixture: ComponentFixture<OmniChainVaultsWalletsDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ OmniChainVaultsWalletsDialogComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(OmniChainVaultsWalletsDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
