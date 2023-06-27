import { Injectable } from '@angular/core';
import { HttpRequest, HttpHandler, HttpEvent, HttpInterceptor } from '@angular/common/http';
import { Observable } from 'rxjs';
import {Router} from "@angular/router";
import {AuthenticationService} from "./authentication.service";

@Injectable()
export class AuthenticationInterceptor implements HttpInterceptor {
  constructor(private authenticationService: AuthenticationService) { }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // add authorization header with jwt token if available

    let currentSession = this.authenticationService.currentSession;

    if (currentSession && currentSession.sessionSecret) {
      request = request.clone({
        setHeaders: {
          'SocialEngine-SessionSecret': currentSession.sessionSecret
        }
      });
    }

    return next.handle(request);
  }
}
