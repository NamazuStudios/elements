import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NeoTokenDialogUpdatedComponent } from './neo-token-dialog-updated.component';

describe('NeoTokenDialogUpdatedComponent', () => {
  let component: NeoTokenDialogUpdatedComponent;
  let fixture: ComponentFixture<NeoTokenDialogUpdatedComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ NeoTokenDialogUpdatedComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(NeoTokenDialogUpdatedComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
