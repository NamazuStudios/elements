import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CustomizationMenuComponent } from './customization-menu.component';

describe('CustomizationMenuComponent', () => {
  let component: CustomizationMenuComponent;
  let fixture: ComponentFixture<CustomizationMenuComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CustomizationMenuComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CustomizationMenuComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
