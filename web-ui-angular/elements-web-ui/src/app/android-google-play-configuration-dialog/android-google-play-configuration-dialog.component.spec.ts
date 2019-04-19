import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AndroidGooglePlayConfigurationDialogComponent } from './android-google-play-configuration-dialog.component';

describe('AndroidGooglePlayConfigurationDialogComponent', () => {
  let component: AndroidGooglePlayConfigurationDialogComponent;
  let fixture: ComponentFixture<AndroidGooglePlayConfigurationDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AndroidGooglePlayConfigurationDialogComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AndroidGooglePlayConfigurationDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
