import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { UserSelectDialogComponent } from './user-select-dialog.component';

describe('UserSelectDialogComponent', () => {
  let component: UserSelectDialogComponent;
  let fixture: ComponentFixture<UserSelectDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ UserSelectDialogComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UserSelectDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
