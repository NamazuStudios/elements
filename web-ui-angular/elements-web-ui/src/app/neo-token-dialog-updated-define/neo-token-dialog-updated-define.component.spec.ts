import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NeoTokenDialogUpdatedDefineComponent } from './neo-token-dialog-updated-define.component';

describe('NeoTokenDialogUpdatedDefineComponent', () => {
  let component: NeoTokenDialogUpdatedDefineComponent;
  let fixture: ComponentFixture<NeoTokenDialogUpdatedDefineComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ NeoTokenDialogUpdatedDefineComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(NeoTokenDialogUpdatedDefineComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
