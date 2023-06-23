import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GeneratedKeysDialogComponent } from './generated-keys-dialog.component';

describe('GeneratedKeysDialogComponent', () => {
  let component: GeneratedKeysDialogComponent;
  let fixture: ComponentFixture<GeneratedKeysDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ GeneratedKeysDialogComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(GeneratedKeysDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
