import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NeoTokensListComponent } from './neo-tokens-list.component';

describe('NeoTokensListComponent', () => {
  let component: NeoTokensListComponent;
  let fixture: ComponentFixture<NeoTokensListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ NeoTokensListComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(NeoTokensListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
