import {AbstractControl, AsyncValidatorFn} from '@angular/forms';
import {ItemsService} from './api/services/items.service';
import { map as __map, filter as __filter } from 'rxjs/operators';

export function itemExistsValidator(itemsService: ItemsService): AsyncValidatorFn {
  return (control: AbstractControl): Promise<{[key: string]: any} | null > => {
    // query to see if control.value represents valid item name

  };
}
