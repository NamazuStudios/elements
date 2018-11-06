import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';

import { AuthenticationService } from "../authentication.service";
import { AlertService } from "../alert.service";
import {MatSnackBar } from "@angular/material";
import {Subscription} from "rxjs";
import {first} from "rxjs/operators";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";

@Component({
  selector: 'login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})

export class LoginComponent implements OnInit, OnDestroy {

  loginForm: FormGroup;
  loading = false;
  submitted = false;
  returnUrl: string;
  alertSubscription: Subscription;
  paramMapSubscription: Subscription;

  get f() { return this.loginForm.controls; }

  constructor(private formBuilder: FormBuilder, private route: ActivatedRoute, private router: Router, private authenticationService: AuthenticationService, private alertService: AlertService, private snackBar: MatSnackBar) {
  }

  ngOnInit() {
    this.loginForm = this.formBuilder.group({
      userId: ['', Validators.required],
      password: ['', Validators.required]
    });

    // reset login status
    this.authenticationService.logout();

    // get return url from route parameters or default to '/'
    this.paramMapSubscription = this.route.queryParams.subscribe(params => this.returnUrl = params.returnUrl || '/');

    this.alertSubscription = this.alertService.getMessage().subscribe((message: any) => {
      if(message) {
        this.snackBar.open(message.text, "Dismiss", { duration: 3000 });
      }
    });
  }

  ngOnDestroy() {
    this.alertSubscription.unsubscribe();
    this.paramMapSubscription.unsubscribe();
  }

  login() {
    this.submitted = true;

    if(this.loginForm.invalid)
      return;

    this.loading = true;

    this.authenticationService.login(this.f.userId.value, this.f.password.value)
      .pipe(first())
      .subscribe(result => {
          this.router.navigate([this.returnUrl]);
          this.alertService.success("login success");
        },
        err => {
          this.alertService.error(err);
          this.loading = false;
      });
  }
}
