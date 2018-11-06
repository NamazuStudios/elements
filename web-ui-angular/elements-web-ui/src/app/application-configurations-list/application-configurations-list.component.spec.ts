import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ApplicationConfigurationsListComponent } from './application-configurations-list.component';

describe('ApplicationConfigurationsListComponent', () => {
  let component: ApplicationConfigurationsListComponent;
  let fixture: ComponentFixture<ApplicationConfigurationsListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ApplicationConfigurationsListComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ApplicationConfigurationsListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
