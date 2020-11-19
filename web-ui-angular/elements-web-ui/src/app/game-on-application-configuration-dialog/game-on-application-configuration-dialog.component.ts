import {Component, Inject, OnInit} from '@angular/core';
import {FormBuilder, Validators} from "@angular/forms";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";

@Component({
  selector: 'app-game-on-application-configuration-dialog',
  templateUrl: './game-on-application-configuration-dialog.component.html',
  styleUrls: ['./game-on-application-configuration-dialog.component.css']
})
export class GameOnApplicationConfigurationDialogComponent implements OnInit {

  public applicationId: string;
  public configurationId: string;

  configurationForm = this.formBuilder.group({
    category: [ 'AMAZON_GAME_ON' ],
    gameId: [ this.data.applicationConfiguration.gameId, Validators.required ],
    publicApiKey: [ this.data.applicationConfiguration.publicApiKey, Validators.required ],
    adminApiKey: [ this.data.applicationConfiguration.adminApiKey, Validators.required ],
    publicKey: [ this.data.applicationConfiguration.publicKey ],
    parent: this.formBuilder.group({
      id: [ this.data.applicationConfiguration.parent.id ]
    })
  });

  constructor(public dialogRef: MatDialogRef<GameOnApplicationConfigurationDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any,
              private formBuilder: FormBuilder) {
    this.applicationId = data.applicationConfiguration.parent.id;
    this.configurationId = data.applicationConfiguration.gameId;
  }

  ngOnInit() {
  }

}
