import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TokenDefinationDuplicateDialogComponent } from './token-defination-duplicate-dialog.component';

describe('TokenDefinationDuplicateDialogComponent', () => {
  let component: TokenDefinationDuplicateDialogComponent;
  let fixture: ComponentFixture<TokenDefinationDuplicateDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TokenDefinationDuplicateDialogComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TokenDefinationDuplicateDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
