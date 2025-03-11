import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Oauth2AuthSchemeDialogComponent } from './oauth2-auth-scheme-dialog.component';

describe('Oauth2AuthSchemeDialogComponent', () => {
  let component: Oauth2AuthSchemeDialogComponent;
  let fixture: ComponentFixture<Oauth2AuthSchemeDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ Oauth2AuthSchemeDialogComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(Oauth2AuthSchemeDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
