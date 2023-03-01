import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OmniChainContractsComponent } from './omni-chain-contracts.component';

describe('OmniChainContractsComponent', () => {
  let component: OmniChainContractsComponent;
  let fixture: ComponentFixture<OmniChainContractsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ OmniChainContractsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(OmniChainContractsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
