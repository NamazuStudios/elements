import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'lastSegmentCapitalize'
})
export class LastSegmentCapitalizePipe implements PipeTransform {
  transform(value: string): string {
    if (!value || typeof value !== 'string') return '';

    const parts = value.split('.');
    const last = parts[parts.length - 1];

    return last.charAt(0).toUpperCase() + last.slice(1);
  }
}
