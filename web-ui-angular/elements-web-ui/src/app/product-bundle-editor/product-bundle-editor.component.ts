import {Component, Inject, OnInit, ViewChild} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material';
import {FormBuilder, Validators} from '@angular/forms';
import {JsonEditorCardComponent} from '../json-editor-card/json-editor-card.component';

@Component({
  selector: 'app-product-bundle-editor',
  templateUrl: './product-bundle-editor.component.html',
  styleUrls: ['./product-bundle-editor.component.css']
})
export class ProductBundleEditorComponent implements OnInit {
  @ViewChild(JsonEditorCardComponent) editorCard: JsonEditorCardComponent;

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

  close(saveChanges: boolean = false) {
    if (!saveChanges) {
      this.dialogRef.close();
      return;
    }

    const formData = this.productBundleForm.value;
    if (this.data.productBundle !== undefined) {
      formData.metadata = this.data.productBundle.metadata;
    }

    this.data.next(formData);
    this.dialogRef.close();
  }

}
