import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from "rxjs";
import {AlertService} from "./alert.service";
import {Router} from "@angular/router";
import { UsernamePasswordSessionService } from './api/services/username-password-session.service'
import {map} from "rxjs/operators";
import {SessionCreation} from "./api/models/session-creation";

@Injectable({
  providedIn: 'root'
})
export class AuthenticationService {
  isLoggedInSubject = new BehaviorSubject<boolean>(this.hasUser());
  currentSessionSubject = new BehaviorSubject<SessionCreation>(this.currentSession);

  constructor(private router: Router, private alertService: AlertService, private usernamePasswordSessionService: UsernamePasswordSessionService) { }

  public hasUser() : boolean {
    return !!localStorage.getItem('currentUser');
  }

  public isLoggedIn() : Observable<boolean> {
    return this.isLoggedInSubject.asObservable();
  }

  public login(username: string, password: string) {
    return this.usernamePasswordSessionService.createSession({ userId: username, password: password })
      .pipe(map(sessionCreation => {
        localStorage.setItem('currentUser', JSON.stringify(sessionCreation));

        this.currentSessionSubject.next(sessionCreation);
        this.isLoggedInSubject.next(true);
      }));
  }

  get currentSession() : SessionCreation {
    try {
      return this.hasUser ? JSON.parse(localStorage.getItem('currentUser')) : null;
    } catch (ex) {
      console.error(ex);
      return null;
    }
  }

  public logout() {
    localStorage.removeItem('currentUser');
    this.isLoggedInSubject.next(false);
    this.currentSessionSubject.next(null);
  }
}
