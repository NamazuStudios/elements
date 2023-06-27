import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OmniChainVaultsComponent } from './omni-chain-vaults.component';

describe('OmniChainVaultsComponent', () => {
  let component: OmniChainVaultsComponent;
  let fixture: ComponentFixture<OmniChainVaultsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ OmniChainVaultsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(OmniChainVaultsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
