import {ProductBundle, ProductBundleReward} from '../api/models/product-bundle';

export class ProductBundleViewModel implements ProductBundle {
  productId: string;
  displayName: string;
  description: string;
  productBundleRewards: Array<ProductBundleReward>;
  metadata?: {[key: string]: any};
  display: boolean;

  constructor() {
    this.productBundleRewards = [];
    this.metadata = {};
    this.display = true;
  }
}
