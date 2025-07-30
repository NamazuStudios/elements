import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MetadataSpecsComponent } from './metadata-specs.component';

describe('MetadataSpecsComponent', () => {
  let component: MetadataSpecsComponent;
  let fixture: ComponentFixture<MetadataSpecsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ MetadataSpecsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MetadataSpecsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
