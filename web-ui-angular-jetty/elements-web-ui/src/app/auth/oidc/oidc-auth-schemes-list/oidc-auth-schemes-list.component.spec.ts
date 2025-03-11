import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OidcAuthSchemesListComponent } from './oidc-auth-schemes-list.component';

describe('OidcAuthSchemesListComponent', () => {
  let component: OidcAuthSchemesListComponent;
  let fixture: ComponentFixture<OidcAuthSchemesListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ OidcAuthSchemesListComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(OidcAuthSchemesListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
