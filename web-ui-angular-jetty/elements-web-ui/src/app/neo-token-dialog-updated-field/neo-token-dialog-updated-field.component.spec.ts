import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NeoTokenDialogUpdatedFieldComponent } from './neo-token-dialog-updated-field.component';

describe('NeoTokenDialogUpdatedFieldComponent', () => {
  let component: NeoTokenDialogUpdatedFieldComponent;
  let fixture: ComponentFixture<NeoTokenDialogUpdatedFieldComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ NeoTokenDialogUpdatedFieldComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(NeoTokenDialogUpdatedFieldComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
