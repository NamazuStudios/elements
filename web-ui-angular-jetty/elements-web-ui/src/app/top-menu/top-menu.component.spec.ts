
import { fakeAsync, ComponentFixture, TestBed } from '@angular/core/testing';

import { TopMenuComponent } from './top-menu.component';
import {RouterTestingModule} from '@angular/router/testing';
import {AlertService} from '../alert.service';
import {HttpClientTestingModule} from '@angular/common/http/testing';

describe('TopMenuComponent', () => {
  let component: TopMenuComponent;
  let fixture: ComponentFixture<TopMenuComponent>;

  beforeEach(fakeAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ TopMenuComponent ],
      imports: [RouterTestingModule, HttpClientTestingModule],
      providers: [
        AlertService
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TopMenuComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should compile', () => {
    expect(component).toBeTruthy();
  });
});
