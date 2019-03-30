import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ProductBundleRewardsEditorComponent } from './product-bundle-rewards-editor.component';

describe('ProductBundleRewardsEditorComponent', () => {
  let component: ProductBundleRewardsEditorComponent;
  let fixture: ComponentFixture<ProductBundleRewardsEditorComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ProductBundleRewardsEditorComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProductBundleRewardsEditorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
