import {Component, Input, OnInit} from '@angular/core';
import {ApplicationConfigurationsDataSource} from '../application-configuration.datasource';
import {ProductBundle} from '../api/models/product-bundle';
import {filter} from 'rxjs/operators';
import {ConfirmationDialogService} from '../confirmation-dialog/confirmation-dialog.service';
import {MatDialog} from '@angular/material';
import {ProductBundleEditorComponent} from '../product-bundle-editor/product-bundle-editor.component';

@Component({
  selector: 'app-product-bundle-list',
  templateUrl: './product-bundle-list.component.html',
  styleUrls: ['./product-bundle-list.component.css']
})
export class ProductBundleListComponent implements OnInit {
  @Input() productBundles: Array<ProductBundle>;

  dataSource: ApplicationConfigurationsDataSource;
  displayedColumns = ["select", "id", "displayName", "display", "actions"];

  constructor(private dialogService: ConfirmationDialogService,
              public dialog: MatDialog) { }

  editProductBundle(productBundle: ProductBundle) {
    this.showDialog(false, ProductBundleEditorComponent, productBundle, res => {
      for (const prop in res) {
        if (res.hasOwnProperty(prop)) {
          productBundle[prop] = res[prop];
        }
      }
    });
  }

  deleteProductBundle(productBundle: ProductBundle) {

  }

  showDialog(isNew: boolean, dialog: any, productBundle: ProductBundle, next) {
    const dialogRef = this.dialog.open(dialog, {
      width: '800px',
      data: { isNew: isNew, productBundle: productBundle, next: next }
    });

    dialogRef
      .afterClosed()
      .pipe(filter(r => r))
      .subscribe(next);
  }

  ngOnInit() {
  }

}
