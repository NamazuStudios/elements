import {AbstractControl, AsyncValidator, ValidationErrors} from '@angular/forms';
import {LeaderboardsService} from '../api/services/leaderboards.service';
import {Observable, of as __of} from 'rxjs';
import {catchError as __catchError, map as __map, tap} from 'rxjs/operators';
import {APIError} from '../api/models/api-error'

export class LeaderboardExistsValidator implements AsyncValidator {
  constructor(private leaderboardsService: LeaderboardsService) {
    this.validate = this.validate.bind(this);
  }

  private handleError(error: APIError): Observable<ValidationErrors> {
    if(error.code === 'NOT_FOUND') {  // we're making sure that the name doesn't already exist
        return __of(null);
    }
    return __of({itemNameNotFound: {value: ''}});
  }

  validate(control: AbstractControl): Promise<ValidationErrors | null> | Observable<ValidationErrors | null> {
    return this.leaderboardsService.getLeaderboard(control.value).pipe(
        __map(item => {
          return item ? {unique: {value: control.value}} : null; }),
        __catchError(this.handleError)
      );
  }
}
