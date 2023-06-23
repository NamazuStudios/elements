import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TokenViewerDialogComponent } from './token-viewer-dialog.component';

describe('TokenViewerDialogComponent', () => {
  let component: TokenViewerDialogComponent;
  let fixture: ComponentFixture<TokenViewerDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TokenViewerDialogComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TokenViewerDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
