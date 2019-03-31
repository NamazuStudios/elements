export interface ProductBundle {
  productId: string;
  displayName: string;
  description: string;
  productBundleRewards: Array<ProductBundleReward>;
  metadata?: {[key: string]: any};
  display: boolean;
}

export interface ProductBundleReward {
  itemId: string;
  quantity: number;
}
