import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OmniChainWalletsDialogComponent } from './omni-chain-wallets-dialog.component';

describe('OmniChainWalletsDialogComponent', () => {
  let component: OmniChainWalletsDialogComponent;
  let fixture: ComponentFixture<OmniChainWalletsDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ OmniChainWalletsDialogComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(OmniChainWalletsDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
