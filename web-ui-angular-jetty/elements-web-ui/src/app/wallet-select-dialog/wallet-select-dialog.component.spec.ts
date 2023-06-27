import { ComponentFixture, TestBed } from '@angular/core/testing';

import { WalletSelectDialogComponent } from './wallet-select-dialog.component';

describe('WalletSelectDialogComponent', () => {
  let component: WalletSelectDialogComponent;
  let fixture: ComponentFixture<WalletSelectDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ WalletSelectDialogComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(WalletSelectDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
