import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Oauth2AuthSchemesListComponent } from './oauth2-auth-schemes-list.component';

describe('Oauth2AuthSchemesListComponent', () => {
  let component: Oauth2AuthSchemesListComponent;
  let fixture: ComponentFixture<Oauth2AuthSchemesListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ Oauth2AuthSchemesListComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(Oauth2AuthSchemesListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
