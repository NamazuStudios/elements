import {ComponentFixture, TestBed, waitForAsync} from '@angular/core/testing';

import { MetadataDialogComponent } from './metadata-dialog.component';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from "@angular/material/dialog";
import {FormBuilder, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {AlertService} from "../../../alert.service";
import {RouterTestingModule} from "@angular/router/testing";
import {MatSnackBar} from "@angular/material/snack-bar";

describe('ItemDialogComponent', () => {
  let component: MetadataDialogComponent;
  let fixture: ComponentFixture<MetadataDialogComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ MetadataDialogComponent ],
      imports: [MatDialogModule, FormsModule, ReactiveFormsModule, RouterTestingModule],
      providers: [
        FormBuilder,
        AlertService,
        MatSnackBar,
        { provide: MatDialogRef, useValue: {} },
        { provide: MAT_DIALOG_DATA, useValue: { item: {
              name: 'metadataName',
              accessLevel: 'accessLevel',
              metadata: {
                meta: 'meta'
              },
              metadataSpec: {}
            }}}
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MetadataDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
