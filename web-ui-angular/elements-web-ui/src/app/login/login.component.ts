import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';

import { AuthenticationService } from "../authentication.service";
import { AlertService } from "../alert.service";
import {MatSnackBar } from "@angular/material";
import {Subscription} from "rxjs";
import {first} from "rxjs/operators";

@Component({
  selector: 'login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})

export class LoginComponent implements OnInit, OnDestroy {

  username: string;
  password: string;
  returnUrl: string;
  alertSubscription: Subscription;
  paramMapSubscription: Subscription;

  constructor(private route: ActivatedRoute, private router: Router, private authenticationService: AuthenticationService, private alertService: AlertService, private snackBar: MatSnackBar) {
  }

  ngOnInit() {
    // reset login status
    this.authenticationService.logout();

    // get return url from route parameters or default to '/'
    this.paramMapSubscription = this.route.queryParams.subscribe(params => this.returnUrl = params.returnUrl || '/');

    this.alertSubscription = this.alertService.getMessage().subscribe((message: any) => {
      if(message) {
        this.snackBar.open(message.text, message.type, { duration: 3000 });
      }
    });
  }

  ngOnDestroy() {
    this.alertSubscription.unsubscribe();
    this.paramMapSubscription.unsubscribe();
  }

  login() : void {
    this.authenticationService.login(this.username, this.password)
      .pipe(first())
      .subscribe(result => {
          this.router.navigate([this.returnUrl]);
          this.alertService.success("login success");
        },
        err => {
          this.alertService.error(err);
      });
  }
}
