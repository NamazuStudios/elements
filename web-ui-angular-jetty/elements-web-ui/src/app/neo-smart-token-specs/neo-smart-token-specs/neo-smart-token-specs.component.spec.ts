import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NeoSmartTokenSpecsComponent } from './neo-smart-token-specs.component';

describe('NeoSmartTokenSpecsComponent', () => {
  let component: NeoSmartTokenSpecsComponent;
  let fixture: ComponentFixture<NeoSmartTokenSpecsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ NeoSmartTokenSpecsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(NeoSmartTokenSpecsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
