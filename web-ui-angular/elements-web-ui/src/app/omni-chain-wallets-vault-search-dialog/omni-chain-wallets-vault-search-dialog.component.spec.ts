import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OmniChainWalletsVaultSearchDialogComponent } from './omni-chain-wallets-vault-search-dialog.component';

describe('OmniChainWalletsVaultSearchDialogComponent', () => {
  let component: OmniChainWalletsVaultSearchDialogComponent;
  let fixture: ComponentFixture<OmniChainWalletsVaultSearchDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ OmniChainWalletsVaultSearchDialogComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(OmniChainWalletsVaultSearchDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
