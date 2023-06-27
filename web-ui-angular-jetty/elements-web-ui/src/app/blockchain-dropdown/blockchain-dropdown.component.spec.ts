import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BlockchainDropdownComponent } from './blockchain-dropdown.component';

describe('BlockchainDropdownComponent', () => {
  let component: BlockchainDropdownComponent;
  let fixture: ComponentFixture<BlockchainDropdownComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ BlockchainDropdownComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(BlockchainDropdownComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
