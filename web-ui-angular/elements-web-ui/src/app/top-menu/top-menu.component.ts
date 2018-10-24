import {Router} from "@angular/router";
import { Component } from '@angular/core';
import { BreakpointObserver, Breakpoints, BreakpointState } from '@angular/cdk/layout';
import { Observable } from "rxjs";
import { map } from 'rxjs/operators';
import {AuthenticationService} from "../authentication.service";

@Component({
  selector: 'top-menu',
  templateUrl: './top-menu.component.html',
  styleUrls: ['./top-menu.component.css']
})
export class TopMenuComponent {

  isLoggedIn$: Observable<boolean>;

  isHandset$: Observable<boolean> = this.breakpointObserver.observe(Breakpoints.Handset)
    .pipe(
      map(result => result.matches)
    );


  constructor(private router: Router, private breakpointObserver: BreakpointObserver, private authenticationService: AuthenticationService) {
    this.isLoggedIn$ = authenticationService.isLoggedIn();
  }
}
