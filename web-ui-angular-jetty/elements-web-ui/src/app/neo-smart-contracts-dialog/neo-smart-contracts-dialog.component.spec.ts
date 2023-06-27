import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NeoSmartContractsDialogComponent } from './neo-smart-contracts-dialog.component';

describe('NeoSmartContractsDialogComponent', () => {
  let component: NeoSmartContractsDialogComponent;
  let fixture: ComponentFixture<NeoSmartContractsDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ NeoSmartContractsDialogComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(NeoSmartContractsDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
