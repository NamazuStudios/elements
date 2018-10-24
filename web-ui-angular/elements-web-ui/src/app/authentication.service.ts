import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from "rxjs";
import {AlertService} from "./alert.service";
import {Router} from "@angular/router";
import { UsernamePasswordSessionService } from './api/services/username-password-session.service'
import {UsernamePasswordSessionRequest} from "./api/models";

@Injectable({
  providedIn: 'root'
})
export class AuthenticationService {
  isLoginSubject = new BehaviorSubject<boolean>(this.hasUser());

  constructor(private router: Router, private alertService: AlertService, private usernamePasswordSessionService: UsernamePasswordSessionService) { }

  public hasUser() : boolean {
    return !!localStorage.getItem('currentUser');
  }

  public isLoggedIn() : Observable<boolean> {
    return this.isLoginSubject.asObservable();
  }

  public login(username: string, password: string): boolean {

    this.usernamePasswordSessionService.createSession({ userId: username, password: password }).subscribe(sessionCreation => {
      console.log(sessionCreation);
      localStorage.setItem('currentUser', JSON.stringify(sessionCreation));

      this.alertService.success("login success");
      this.isLoginSubject.next(true);
    });

    return this.hasUser();
  }

  public logout() {
    localStorage.removeItem('currentUser');
    this.isLoginSubject.next(false);
  }
}
