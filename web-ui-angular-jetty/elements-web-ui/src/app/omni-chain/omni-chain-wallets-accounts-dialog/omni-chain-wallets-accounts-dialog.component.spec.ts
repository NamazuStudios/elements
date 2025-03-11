import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OmniChainWalletsAccountsDialogComponent } from './omni-chain-wallets-accounts-dialog.component';

describe('OmniChainWalletsAccountsDialogComponent', () => {
  let component: OmniChainWalletsAccountsDialogComponent;
  let fixture: ComponentFixture<OmniChainWalletsAccountsDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ OmniChainWalletsAccountsDialogComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(OmniChainWalletsAccountsDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
