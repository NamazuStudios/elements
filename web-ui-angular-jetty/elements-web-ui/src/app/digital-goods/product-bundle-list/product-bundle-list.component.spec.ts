import {ComponentFixture, TestBed, waitForAsync} from '@angular/core/testing';

import { ProductBundleListComponent } from './product-bundle-list.component';
import {ConfirmationDialogService} from '../../confirmation-dialog/confirmation-dialog.service';
import {MatDialog, MatDialogModule} from '@angular/material/dialog';
import {MatPaginator, MatPaginatorModule} from '@angular/material/paginator';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';

describe('ProductBundleListComponent', () => {
  let component: ProductBundleListComponent;
  let fixture: ComponentFixture<ProductBundleListComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ ProductBundleListComponent ],
      imports: [MatDialogModule, MatPaginatorModule, BrowserAnimationsModule],
      providers: [
        ConfirmationDialogService,
        MatDialog,
        MatPaginator
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProductBundleListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
