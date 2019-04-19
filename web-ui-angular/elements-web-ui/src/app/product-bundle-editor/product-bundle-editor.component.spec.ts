import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ProductBundleEditorComponent } from './product-bundle-editor.component';

describe('ProductBundleEditorComponent', () => {
  let component: ProductBundleEditorComponent;
  let fixture: ComponentFixture<ProductBundleEditorComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ProductBundleEditorComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProductBundleEditorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
