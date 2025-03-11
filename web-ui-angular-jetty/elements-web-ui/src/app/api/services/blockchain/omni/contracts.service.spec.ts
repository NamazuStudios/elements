import { TestBed } from '@angular/core/testing';

import { ContractsService } from './contracts.service';

describe('ContractsService', () => {
  let service: ContractsService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ContractsService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
