import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OmniChainVaultsDialogComponent } from './omni-chain-vaults-dialog.component';

describe('OmniChainVaultsDialogComponent', () => {
  let component: OmniChainVaultsDialogComponent;
  let fixture: ComponentFixture<OmniChainVaultsDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ OmniChainVaultsDialogComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(OmniChainVaultsDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
