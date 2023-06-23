import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TokenViewLightboxDialogComponent } from './token-view-lightbox-dialog.component';

describe('TokenViewLightboxDialogComponent', () => {
  let component: TokenViewLightboxDialogComponent;
  let fixture: ComponentFixture<TokenViewLightboxDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TokenViewLightboxDialogComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TokenViewLightboxDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
