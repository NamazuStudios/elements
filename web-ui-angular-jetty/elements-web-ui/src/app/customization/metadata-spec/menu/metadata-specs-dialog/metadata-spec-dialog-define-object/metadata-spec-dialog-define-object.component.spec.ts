import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MetadataSpecDialogDefineObjectComponent } from './metadata-spec-dialog-define-object.component';

describe('NeoTokenDialogDefineObjectComponent', () => {
  let component: MetadataSpecDialogDefineObjectComponent;
  let fixture: ComponentFixture<MetadataSpecDialogDefineObjectComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ MetadataSpecDialogDefineObjectComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MetadataSpecDialogDefineObjectComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
