import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CustomAuthSchemeDialogComponent } from './custom-auth-scheme-dialog.component';

describe('CustomAuthSchemeDialogComponent', () => {
  let component: CustomAuthSchemeDialogComponent;
  let fixture: ComponentFixture<CustomAuthSchemeDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CustomAuthSchemeDialogComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CustomAuthSchemeDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
