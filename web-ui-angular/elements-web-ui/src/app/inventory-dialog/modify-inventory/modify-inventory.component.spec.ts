import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ModifyInventoryComponent } from './modify-inventory.component';

describe('ModifyInventoryComponent', () => {
  let component: ModifyInventoryComponent;
  let fixture: ComponentFixture<ModifyInventoryComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ModifyInventoryComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ModifyInventoryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
