import {ComponentFixture, TestBed, waitForAsync} from '@angular/core/testing';

import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {FormBuilder, FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatSnackBar} from '@angular/material/snack-bar';
import {RouterTestingModule} from '@angular/router/testing';
import {ApplicationAttributesComponent} from './application-attributes.component';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';

describe('ApplicationAttributesComponent', () => {
  let component: ApplicationAttributesComponent;
  let fixture: ComponentFixture<ApplicationAttributesComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ ApplicationAttributesComponent ],
      imports: [MatDialogModule, FormsModule, ReactiveFormsModule, RouterTestingModule, MatFormFieldModule, MatInputModule],
      providers: [
        FormBuilder,
        { provide: MatDialogRef, useValue: {} },
        { provide: MatSnackBar, useValue: {} },
        { provide: MAT_DIALOG_DATA, useValue: { application: {
              attributes: {
                'key' : 'value'
              },
            }}},
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ApplicationAttributesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
