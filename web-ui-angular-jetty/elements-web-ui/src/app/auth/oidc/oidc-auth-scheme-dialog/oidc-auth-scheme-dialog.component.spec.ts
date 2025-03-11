import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OidcAuthSchemeDialogComponent } from './oidc-auth-scheme-dialog.component';

describe('OidcAuthSchemeDialogComponent', () => {
  let component: OidcAuthSchemeDialogComponent;
  let fixture: ComponentFixture<OidcAuthSchemeDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ OidcAuthSchemeDialogComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(OidcAuthSchemeDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
