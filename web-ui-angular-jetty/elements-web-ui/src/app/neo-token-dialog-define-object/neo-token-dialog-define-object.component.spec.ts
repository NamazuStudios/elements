import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NeoTokenDialogDefineObjectComponent } from './neo-token-dialog-define-object.component';

describe('NeoTokenDialogDefineObjectComponent', () => {
  let component: NeoTokenDialogDefineObjectComponent;
  let fixture: ComponentFixture<NeoTokenDialogDefineObjectComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ NeoTokenDialogDefineObjectComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(NeoTokenDialogDefineObjectComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
