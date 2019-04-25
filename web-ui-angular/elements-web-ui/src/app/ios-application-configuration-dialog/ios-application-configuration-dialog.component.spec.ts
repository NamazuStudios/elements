import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { IosApplicationConfigurationDialogComponent } from './ios-application-configuration-dialog.component';

describe('IosApplicationConfigurationDialogComponent', () => {
  let component: IosApplicationConfigurationDialogComponent;
  let fixture: ComponentFixture<IosApplicationConfigurationDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ IosApplicationConfigurationDialogComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(IosApplicationConfigurationDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
