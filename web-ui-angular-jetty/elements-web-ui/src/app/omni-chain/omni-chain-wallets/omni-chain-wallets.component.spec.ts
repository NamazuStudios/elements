import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OmniChainWalletsComponent } from './omni-chain-wallets.component';

describe('OmniChainWalletsComponent', () => {
  let component: OmniChainWalletsComponent;
  let fixture: ComponentFixture<OmniChainWalletsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ OmniChainWalletsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(OmniChainWalletsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
