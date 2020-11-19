import { Component, Input, OnInit } from '@angular/core';
import {GameOnPrizesDataSource} from "../game-on-prizes.datasource";
import {MatDialog} from "@angular/material/dialog";
import {AlertService} from "../alert.service";
import {GameOnPrizeDialogComponent} from "../game-on-prize-dialog/game-on-prize-dialog.component";
import {filter} from "rxjs/operators";
import {GameOnPrizesService} from "../api/services/game-on-prizes.service";
import {Prize} from "../api/models/prize";
import {PrizeViewModel} from "../models/prize-view-model";

@Component({
  selector: 'app-game-on-prizes-list',
  templateUrl: './game-on-prizes-list.component.html',
  styleUrls: ['./game-on-prizes-list.component.css']
})
export class GameOnPrizesListComponent implements OnInit {
  @Input() applicationId: string;
  @Input() configurationId: string;

  dataSource: GameOnPrizesDataSource;
  displayedColumns= [ "prizeId", "title" ];

  constructor(private gameOnPrizesService: GameOnPrizesService, private alertService: AlertService, public dialog: MatDialog) { }

  ngOnInit() {
    this.dataSource = new GameOnPrizesDataSource(this.gameOnPrizesService);
    this.refresh(0);
  }

  refresh(delay = 500) {
    setTimeout(() => {
      this.dataSource.loadGameOnPrizes(this.applicationId, this.configurationId);
    }, delay)
  }

  showDialog(isNew: boolean, gameOnPrize: Prize, next) {
    const dialogRef = this.dialog.open(GameOnPrizeDialogComponent, {
      width: '900px',
      data: { isNew: isNew, gameOnPrize: gameOnPrize }
    });

    dialogRef
      .afterClosed()
      .pipe(filter(r => r))
      .subscribe(next);
  }

  addGameOnPrize() {
    this.showDialog(true, new PrizeViewModel(),result => {
      this.gameOnPrizesService.createPrizes({ applicationId: this.applicationId, configurationId: this.configurationId, body: { prizes: [ result ] } }).subscribe(r => {
          this.refresh();
        },
        error => this.alertService.error(error));
    });
  }
}
