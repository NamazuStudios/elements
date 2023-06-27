import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OmniChainComponent } from './omni-chain.component';

describe('OmniChainComponent', () => {
  let component: OmniChainComponent;
  let fixture: ComponentFixture<OmniChainComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ OmniChainComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(OmniChainComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
