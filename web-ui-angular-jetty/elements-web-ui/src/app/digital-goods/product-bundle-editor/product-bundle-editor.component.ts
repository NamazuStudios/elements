import {Component, Inject, OnInit, ViewChild} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {FormBuilder, Validators} from '@angular/forms';
import {JsonEditorCardComponent} from '../../json-editor-card/json-editor-card.component';
import {BundleRewardsEditorComponent} from '../bundle-rewards-editor/bundle-rewards-editor.component';

@Component({
  selector: 'app-product-bundle-editor',
  templateUrl: './product-bundle-editor.component.html',
  styleUrls: ['./product-bundle-editor.component.css']
})
export class ProductBundleEditorComponent implements OnInit {
  @ViewChild(JsonEditorCardComponent) editorCard: JsonEditorCardComponent;
  @ViewChild(BundleRewardsEditorComponent) bundleRewardsEditor: BundleRewardsEditorComponent;

  productBundleForm = this.formBuilder.group({
    productId: [this.data.productBundle.productId, [Validators.required]],
    displayName: [this.data.productBundle.displayName, [Validators.required]],
    description: [this.data.productBundle.description, [Validators.required]],
    display: [this.data.productBundle.display]
  });

  originalMetadata = JSON.parse(JSON.stringify(this.data.productBundle.metadata || {}));

  constructor(public dialogRef: MatDialogRef<ProductBundleEditorComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any,
              private formBuilder: FormBuilder) { }

  ngOnInit() {
  }

  close(saveChanges: boolean = false) {
    if (!saveChanges) {
      this.data.productBundle.metadata = this.originalMetadata;
      this.dialogRef.close();
      return;
    }

    this.editorCard.validateMetadata(true);

    const formData = this.productBundleForm.value;
    if (this.data.productBundle !== undefined) {
      formData.metadata = this.data.productBundle.metadata;

      formData.productBundleRewards = this.bundleRewardsEditor.getRawRewards();
    }

    this.data.next(formData);
    this.dialogRef.close(formData);
  }

}
