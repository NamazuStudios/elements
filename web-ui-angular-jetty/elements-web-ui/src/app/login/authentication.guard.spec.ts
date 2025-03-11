import { TestBed, inject } from '@angular/core/testing';

import { AuthenticationGuard } from './authentication.guard';
import {RouterTestingModule} from "@angular/router/testing";
import {AlertService} from "../alert.service";
import {HttpClientTestingModule} from "@angular/common/http/testing";

describe('Authentication2Guard', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [RouterTestingModule, HttpClientTestingModule],
      providers: [
        AuthenticationGuard,
        AlertService
      ]
    });
  });

  it('should ...', inject([AuthenticationGuard], (guard: AuthenticationGuard) => {
    expect(guard).toBeTruthy();
  }));
});
