import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OmniChainUserSearchDialogComponent } from './omni-chain-user-search-dialog.component';

describe('OmniChainUserSearchDialogComponent', () => {
  let component: OmniChainUserSearchDialogComponent;
  let fixture: ComponentFixture<OmniChainUserSearchDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ OmniChainUserSearchDialogComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(OmniChainUserSearchDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
