import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RegenerateKeysDialogComponent } from './regenerate-keys-dialog.component';

describe('RegenerateKeysDialogComponent', () => {
  let component: RegenerateKeysDialogComponent;
  let fixture: ComponentFixture<RegenerateKeysDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RegenerateKeysDialogComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(RegenerateKeysDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
