import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AuthSchemeDialogComponent } from './auth-scheme-dialog.component';

describe('AuthSchemeDialogComponent', () => {
  let component: AuthSchemeDialogComponent;
  let fixture: ComponentFixture<AuthSchemeDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AuthSchemeDialogComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AuthSchemeDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
