import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { FacebookApplicationConfigurationDialogComponent } from './facebook-application-configuration-dialog.component';

describe('FacebookApplicationConfigurationsDialogComponent', () => {
  let component: FacebookApplicationConfigurationDialogComponent;
  let fixture: ComponentFixture<FacebookApplicationConfigurationDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ FacebookApplicationConfigurationDialogComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(FacebookApplicationConfigurationDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
