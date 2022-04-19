import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NeoTokenDialogHubComponent } from './neo-token-dialog-hub.component';

describe('NeoTokenDialogHubComponent', () => {
  let component: NeoTokenDialogHubComponent;
  let fixture: ComponentFixture<NeoTokenDialogHubComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ NeoTokenDialogHubComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(NeoTokenDialogHubComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
