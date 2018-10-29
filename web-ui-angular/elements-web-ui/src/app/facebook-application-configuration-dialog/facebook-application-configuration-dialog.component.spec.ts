import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { FacebookApplicationConfigurationsDialogComponent } from './facebook-application-configuration-dialog.component';

describe('FacebookApplicationConfigurationsDialogComponent', () => {
  let component: FacebookApplicationConfigurationsDialogComponent;
  let fixture: ComponentFixture<FacebookApplicationConfigurationsDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ FacebookApplicationConfigurationsDialogComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(FacebookApplicationConfigurationsDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
