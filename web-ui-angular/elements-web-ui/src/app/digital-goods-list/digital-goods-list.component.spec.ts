import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DigitalGoodsListComponent } from './digital-goods-list.component';

describe('DigitalGoodsListComponent', () => {
  let component: DigitalGoodsListComponent;
  let fixture: ComponentFixture<DigitalGoodsListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DigitalGoodsListComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DigitalGoodsListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
