import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material';
import {FormBuilder, Validators} from '@angular/forms';

@Component({
  selector: 'app-product-bundle-editor',
  templateUrl: './product-bundle-editor.component.html',
  styleUrls: ['./product-bundle-editor.component.css']
})
export class ProductBundleEditorComponent implements OnInit {
  productBundleForm = this.formBuilder.group({
    productId: [this.data.productBundle.productId, [Validators.required]],
    displayName: [this.data.productBundle.displayName, [Validators.required]],
    description: [this.data.productBundle.description, [Validators.required]]
  });

  constructor(public dialogRef: MatDialogRef<ProductBundleEditorComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any,
              private formBuilder: FormBuilder) { }

  ngOnInit() {
  }

}
