import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { BundleRewardsEditorComponent } from './bundle-rewards-editor.component';

describe('BundleRewardsEditorComponent', () => {
  let component: BundleRewardsEditorComponent;
  let fixture: ComponentFixture<BundleRewardsEditorComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ BundleRewardsEditorComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BundleRewardsEditorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
