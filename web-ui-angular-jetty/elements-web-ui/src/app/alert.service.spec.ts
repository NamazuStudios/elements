import { TestBed } from '@angular/core/testing';

import { AlertService } from './alert.service';
import {RouterTestingModule} from "@angular/router/testing";

describe('AlertService', () => {
  beforeEach(() => TestBed.configureTestingModule({
    imports: [RouterTestingModule],
    providers: [
      AlertService
    ]
  }));

  it('should be created', () => {
    const service: AlertService = TestBed.inject(AlertService);
    expect(service).toBeTruthy();
  });
});
