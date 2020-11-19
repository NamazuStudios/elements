import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { GameOnPrizesListComponent } from './game-on-prizes-list.component';
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {AlertService} from "../alert.service";
import {RouterTestingModule} from "@angular/router/testing";
import {MatDialog, MatDialogModule} from "@angular/material/dialog";

describe('GameOnApplicationPrizesListComponent', () => {
  let component: GameOnPrizesListComponent;
  let fixture: ComponentFixture<GameOnPrizesListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ GameOnPrizesListComponent ],
      imports: [HttpClientTestingModule, RouterTestingModule, MatDialogModule],
      providers: [AlertService, MatDialog]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(GameOnPrizesListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
