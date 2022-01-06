import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AuthSchemesListComponent } from './auth-schemes-list.component';

describe('AuthSchemesListComponent', () => {
  let component: AuthSchemesListComponent;
  let fixture: ComponentFixture<AuthSchemesListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AuthSchemesListComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AuthSchemesListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
