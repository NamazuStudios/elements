import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { FirebaseApplicationConfigurationDialogComponent } from './firebase-application-configuration-dialog.component';

describe('FirebaseApplicationConfigurationDialogComponent', () => {
  let component: FirebaseApplicationConfigurationDialogComponent;
  let fixture: ComponentFixture<FirebaseApplicationConfigurationDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ FirebaseApplicationConfigurationDialogComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(FirebaseApplicationConfigurationDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
