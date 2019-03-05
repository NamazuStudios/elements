import {AbstractControl, AsyncValidator, ValidationErrors} from '@angular/forms';
import {ItemsService} from './api/services/items.service';
import {Observable} from 'rxjs';
import {map as __map} from 'rxjs/operators';

export class ItemExistsValidator implements AsyncValidator {
  constructor(private itemsService: ItemsService) {}

  validate(control: AbstractControl): Promise<ValidationErrors | null> | Observable<ValidationErrors | null> {
    return this.itemsService.getItemByIdentifier(control.value)
      .pipe(__map(item => item ? null : {itemNameNotFound: {value: control.value}}))
  }
}
