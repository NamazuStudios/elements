import {AbstractControl, AsyncValidator, ValidationErrors} from '@angular/forms';
import {ItemsService} from './api/services/items.service';
import {Observable, of as __of} from 'rxjs';
import {catchError as __catchError, map as __map} from 'rxjs/operators';
import {HttpErrorResponse} from '@angular/common/http';

export class ItemExistsValidator implements AsyncValidator {
  constructor(private itemsService: ItemsService) {
    this.validate = this.validate.bind(this);
  }

  private handleError(error: HttpErrorResponse): Observable<ValidationErrors> {
    return __of({itemNameNotFound: {value: ''}});
  }

  validate(control: AbstractControl): Promise<ValidationErrors | null> | Observable<ValidationErrors | null> {
    return this.itemsService.getItemByIdentifier(control.value).pipe(
        __map(item => {
          console.log(item);
          return item ? null : {itemNameNotFound: {value: control.value}}; }),
        __catchError(this.handleError)
      );
  }
}
