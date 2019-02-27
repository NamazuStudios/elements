import {AbstractControl, AsyncValidatorFn} from '@angular/forms';
import {ItemsService} from './api/services/items.service';

export function itemExistsValidator(itemsService: ItemsService): AsyncValidatorFn {
  return async (control: AbstractControl): Promise<{[key: string]: any} | null > => {
    // TODO query to see if control.value represents valid item name
    const item = await itemsService.getItemByIdentifier(control.value);
    console.log(item);
    return item ? null : {'itemNameNotFound': {value: control.value}};
  };
}
