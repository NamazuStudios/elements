import { Injectable } from '@angular/core';
import { HttpRequest, HttpHandler, HttpEvent, HttpInterceptor } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { AuthenticationService } from './authentication.service';
import { APIError } from './api/models/api-error';

@Injectable()
export class ApiErrorInterceptor implements HttpInterceptor {
  constructor(private authenticationService: AuthenticationService) {}

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(request).pipe(catchError(err => {
      if (err.status === 401 || err.status === 403) {
        // auto logout if 401 or 403 response returned from api
        this.authenticationService.logout();
        location.reload();
      }

      const error: APIError = {
        code: err.error.code,
        message: err.error.message || err.statusText
      };
      return throwError(error);
    }))
  }
}
