import { Component, OnInit } from '@angular/core';
import {ApplicationConfigurationsDataSource} from '../application-configuration.datasource';

@Component({
  selector: 'app-product-bundle-list',
  templateUrl: './product-bundle-list.component.html',
  styleUrls: ['./product-bundle-list.component.css']
})
export class ProductBundleListComponent implements OnInit {
  dataSource: ApplicationConfigurationsDataSource;
  displayedColumns = ["select", "id", "displayName", "display", "actions"]
  constructor() { }

  ngOnInit() {
  }

}
