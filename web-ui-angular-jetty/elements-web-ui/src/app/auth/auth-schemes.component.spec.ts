import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AuthSchemesComponent } from './auth-schemes.component';

describe('AuthSchemesComponent', () => {
  let component: AuthSchemesComponent;
  let fixture: ComponentFixture<AuthSchemesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AuthSchemesComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AuthSchemesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
