import {Component, Inject, OnInit} from '@angular/core';
import {FormBuilder, Validators} from "@angular/forms";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";

@Component({
  selector: 'app-game-on-prize-dialog',
  templateUrl: './game-on-prize-dialog.component.html',
  styleUrls: ['./game-on-prize-dialog.component.css']
})
export class GameOnPrizeDialogComponent implements OnInit {

  gameOnPrizeForm = this.formBuilder.group({
    title: [ this.data.gameOnPrize.title, Validators.required ],
    description: [ this.data.gameOnPrize.description, Validators.required ],
    prizeInfo: [ this.data.gameOnPrize.prizeInfo, Validators.required ],
    imageUrl: [ this.data.gameOnPrize.imageUrl, Validators.required ]
  });

  constructor(public dialogRef: MatDialogRef<GameOnPrizeDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any,
              private formBuilder: FormBuilder) { }

  ngOnInit() {
  }

}
