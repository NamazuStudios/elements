import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { LoginComponent } from './login.component';
import {FormBuilder, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {ActivatedRoute, RouterModule} from '@angular/router';
import {RouterTestingModule} from "@angular/router/testing";
import {AlertService} from "../alert.service";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {MatSnackBar, MatSnackBarModule} from "@angular/material/snack-bar";

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ LoginComponent ],
      imports: [FormsModule, ReactiveFormsModule, RouterTestingModule, HttpClientTestingModule, MatSnackBarModule, RouterModule],
      providers: [
        FormBuilder,
        AlertService,
        MatSnackBar,
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
