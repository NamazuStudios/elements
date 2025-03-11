import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CustomAuthSchemesListComponent } from './custom-auth-schemes-list.component';

describe('CustomAuthSchemesListComponent', () => {
  let component: CustomAuthSchemesListComponent;
  let fixture: ComponentFixture<CustomAuthSchemesListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CustomAuthSchemesListComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CustomAuthSchemesListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
