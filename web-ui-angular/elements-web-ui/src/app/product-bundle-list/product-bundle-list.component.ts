import {Component, Input, OnInit} from '@angular/core';
import {ApplicationConfigurationsDataSource} from '../application-configuration.datasource';
import {ProductBundle} from '../api/models/product-bundle';

@Component({
  selector: 'app-product-bundle-list',
  templateUrl: './product-bundle-list.component.html',
  styleUrls: ['./product-bundle-list.component.css']
})
export class ProductBundleListComponent implements OnInit {
  @Input() productBundles: Array<ProductBundle>;

  dataSource: ApplicationConfigurationsDataSource;
  displayedColumns = ["select", "id", "displayName", "display", "actions"];

  constructor() { }

  editProductBundle(productBundle: ProductBundle) {

  }

  deleteProductBundle(productBundle: ProductBundle) {

  }

  ngOnInit() {
  }

}
