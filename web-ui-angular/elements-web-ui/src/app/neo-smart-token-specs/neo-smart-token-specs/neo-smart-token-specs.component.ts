import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { NeoSmartTokenSpecsService } from 'src/app/api/services/blockchain/neo-smart-token-specs.service';
import { NeoSmartTokenSpecsDialogComponent } from 'src/app/neo-smart-token-specs-dialog/neo-smart-token-specs-dialog.component';

@Component({
  selector: 'app-neo-smart-token-specs',
  templateUrl: './neo-smart-token-specs.component.html',
  styleUrls: ['./neo-smart-token-specs.component.css']
})
export class NeoSmartTokenSpecsComponent implements OnInit {

  dataSource;
  displayedColumns: Array<string> = [
    "id",
    "name",
    "contract",
    "edit-action",
    "copy-action",
    "remove-action"
  ];

  constructor(
    private neoSmartTokenSpecsService: NeoSmartTokenSpecsService,
    public dialog: MatDialog,
  ) {}

  ngOnInit() {
    this.dataSource = [
      {
        id: 'tfdvkdmvdfk',
        name: 'Name 1',
        contract: 'Contract 1',
      },
      {
        id: 'kmlkmnklghnmk',
        name: 'Name 2',
        contract: 'Contract 2',
      }
    ];
  }

  showDialog() {
    this.dialog.open(NeoSmartTokenSpecsDialogComponent, {
      width: "800px",
      maxHeight: "90vh",
      data: {},
    });
  }
}
