import { async, ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ProductBundleEditorComponent } from './product-bundle-editor.component';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {FormBuilder, FormControl, FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatSelectModule} from '@angular/material/select';
import {MatFormFieldControl, MatFormFieldModule} from '@angular/material/form-field';
import {MatCheckbox, MatCheckboxModule} from '@angular/material/checkbox';
import {MatInputModule} from '@angular/material/input';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';

describe('ProductBundleEditorComponent', () => {
  let component: ProductBundleEditorComponent;
  let fixture: ComponentFixture<ProductBundleEditorComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ ProductBundleEditorComponent ],
      imports: [MatDialogModule, FormsModule, ReactiveFormsModule, MatInputModule, MatCheckboxModule, BrowserAnimationsModule],
      providers: [
        FormBuilder,
        MatFormFieldControl,
        FormControl,
        MatCheckbox,
        { provide: MatDialogRef, useValue: {} },
        { provide: MAT_DIALOG_DATA, useValue: { productBundle: {
              productId: 1234,
              displayName: 'display name',
              description: 'description',
              display: 0,
              metadata: {
                meta: 1
              }
            }}},
      ]
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
