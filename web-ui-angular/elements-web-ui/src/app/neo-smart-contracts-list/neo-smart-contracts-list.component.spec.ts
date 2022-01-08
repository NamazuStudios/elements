import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NeoSmartContractsListComponent } from './neo-smart-contracts-list.component';

describe('NeoSmartContractsListComponent', () => {
  let component: NeoSmartContractsListComponent;
  let fixture: ComponentFixture<NeoSmartContractsListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ NeoSmartContractsListComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(NeoSmartContractsListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
