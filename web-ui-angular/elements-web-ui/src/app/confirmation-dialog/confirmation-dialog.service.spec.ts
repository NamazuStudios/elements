import { TestBed } from '@angular/core/testing';

import { ConfirmationDialogService } from './confirmation-dialog.service';
import {MatDialogModule} from "@angular/material/dialog";

describe('ConfirmationDialogService', () => {
  beforeEach(() => TestBed.configureTestingModule({
    imports: [MatDialogModule],
    providers: [ConfirmationDialogService]
  }));

  it('should be created', () => {
    const service: ConfirmationDialogService = TestBed.inject(ConfirmationDialogService);
    expect(service).toBeTruthy();
  });
});
