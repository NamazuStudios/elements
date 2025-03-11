import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ItemsListComponent } from './items-list.component';
import {ItemsService} from "../../api/services/items.service";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {AlertService} from "../../alert.service";
import {RouterTestingModule} from "@angular/router/testing";
import {ConfirmationDialogService} from "../../confirmation-dialog/confirmation-dialog.service";
import {MatDialogModule} from "@angular/material/dialog";
import {MatPaginatorModule} from "@angular/material/paginator";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";

describe('ItemsListComponent', () => {
  let component: ItemsListComponent;
  let fixture: ComponentFixture<ItemsListComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ ItemsListComponent ],
      imports: [HttpClientTestingModule, RouterTestingModule, MatDialogModule, MatPaginatorModule, BrowserAnimationsModule],
      providers: [
        ItemsService,
        AlertService,
        ConfirmationDialogService
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ItemsListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
