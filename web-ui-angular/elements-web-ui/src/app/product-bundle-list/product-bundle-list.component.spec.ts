import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ProductBundleListComponent } from './product-bundle-list.component';

describe('ProductBundleListComponent', () => {
  let component: ProductBundleListComponent;
  let fixture: ComponentFixture<ProductBundleListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ProductBundleListComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProductBundleListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
