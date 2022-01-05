import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NeoTokenDialogComponent } from './neo-token-dialog.component';

describe('NeoTokenDialogComponent', () => {
  let component: NeoTokenDialogComponent;
  let fixture: ComponentFixture<NeoTokenDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ NeoTokenDialogComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(NeoTokenDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
