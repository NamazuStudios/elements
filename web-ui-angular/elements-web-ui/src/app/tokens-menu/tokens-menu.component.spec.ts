import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TokensMenuComponent } from './tokens-menu.component';

describe('TokensMenuComponent', () => {
  let component: TokensMenuComponent;
  let fixture: ComponentFixture<TokensMenuComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TokensMenuComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TokensMenuComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
