import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from "rxjs";
import {AlertService} from "./alert.service";
import {Router} from "@angular/router";
import { UsernamePasswordSessionService } from './api/services/username-password-session.service'
import {map} from "rxjs/operators";

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

  public login(username: string, password: string) {
    return this.usernamePasswordSessionService.createSession({ userId: username, password: password })
      .pipe(map(sessionCreation => {
        localStorage.setItem('currentUser', JSON.stringify(sessionCreation));

        this.isLoginSubject.next(true);
      }));
  }

  public logout() {
    localStorage.removeItem('currentUser');
    this.isLoginSubject.next(false);
  }
}
