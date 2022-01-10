import { Router } from "@angular/router";
import { Component, ViewChild } from "@angular/core";
import {
  BreakpointObserver,
  Breakpoints,
  BreakpointState,
} from "@angular/cdk/layout";
import { Observable } from "rxjs";
import { map } from "rxjs/operators";
import { AuthenticationService } from "../authentication.service";
import { MatDrawer, MatSidenav } from "@angular/material/sidenav";

@Component({
  selector: "top-menu",
  templateUrl: "./top-menu.component.html",
  styleUrls: ["./top-menu.component.css"],
})
export class TopMenuComponent {
  @ViewChild('drawer') drawer!: MatSidenav;

  isHandset$: Observable<boolean> = this.breakpointObserver
    .observe(Breakpoints.Handset)
    .pipe(map((result) => result.matches));

  constructor(
    private router: Router,
    private breakpointObserver: BreakpointObserver,
    public authenticationService: AuthenticationService
  ) {}

  toggle(): void {
    //this.sideNav.toggle();
    this.drawer.toggle();
  }
}
