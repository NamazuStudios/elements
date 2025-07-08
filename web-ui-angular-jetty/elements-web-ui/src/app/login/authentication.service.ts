import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from "rxjs";
import {AlertService} from "../alert.service";
import {Router} from "@angular/router";
import { UsernamePasswordSessionService } from '../api/services/username-password-session.service'
import {map} from "rxjs/operators";
import {SessionCreation} from "../api/models/session-creation";

@Injectable({
  providedIn: 'root'
})
export class AuthenticationService {
  isLoggedInSubject = new BehaviorSubject<boolean>(this.hasValidSession());
  currentSessionSubject = new BehaviorSubject<SessionCreation>(this.currentSession);

  constructor(private router: Router, private alertService: AlertService, private usernamePasswordSessionService: UsernamePasswordSessionService) { }

  public hasValidSession() : boolean {
    let user = localStorage.getItem('currentSession');

    if(user) {
      let currentSeconds = new Date().getTime();
      let expiry = JSON.parse(user).session.expiry;

      return expiry > currentSeconds;
    }

    return false;
  }

  public isLoggedIn() : Observable<boolean> {
    return this.isLoggedInSubject.asObservable();
  }

  public login(username: string, password: string) {
    return this.usernamePasswordSessionService.createSession({ userId: username, password: password })
      .pipe(map(sessionCreation => {
        let json = JSON.stringify(sessionCreation)
        // console.log("CREATED SESSION " + json);
        localStorage.setItem('currentSession', json);

        this.currentSessionSubject.next(sessionCreation);
        this.isLoggedInSubject.next(true);
      }));
  }

  get currentSession() : SessionCreation {
    try {
      return this.hasValidSession ? JSON.parse(localStorage.getItem('currentSession')) : null;
    } catch (ex) {
      console.error(ex);
      return null;
    }
  }

  public logout() {
    localStorage.removeItem('currentSession');
    localStorage.removeItem('appConfig');
    this.isLoggedInSubject.next(false);
    this.currentSessionSubject.next(null);
  }
}
