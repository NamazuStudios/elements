import {ComponentFixture, TestBed, waitForAsync} from '@angular/core/testing';

import {MetadataspecSelectDialogComponent} from './metadataspec-select-dialog.component';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {AlertService} from '../../../alert.service';
import {RouterTestingModule} from '@angular/router/testing';
import {MatSnackBar, MatSnackBarModule} from '@angular/material/snack-bar';
import {MatPaginator, MatPaginatorModule} from '@angular/material/paginator';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';

describe('MetadataSelectDialogComponent', () => {
  let component: MetadataspecSelectDialogComponent;
  let fixture: ComponentFixture<MetadataspecSelectDialogComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ MetadataspecSelectDialogComponent ],
      imports: [MatDialogModule, HttpClientTestingModule, RouterTestingModule, MatSnackBarModule, MatPaginatorModule, BrowserAnimationsModule],
      providers: [
        AlertService,
        MatSnackBar,
        MatPaginator,
        { provide: MatDialogRef, useValue: {} },
        { provide: MAT_DIALOG_DATA, useValue: {} }
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MetadataspecSelectDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
