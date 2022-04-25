import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { NeoTokenSpecsService } from 'src/app/api/services/blockchain/neo-token-specs.service';
import { NeoSmartTokenSpecsDialogComponent } from 'src/app/neo-smart-token-specs-dialog/neo-smart-token-specs-dialog.component';
import { NeoTokensSpecDataSource } from 'src/app/neo-tokens-spec.datasource';

@Component({
  selector: 'app-neo-smart-token-specs',
  templateUrl: './neo-smart-token-specs.component.html',
  styleUrls: ['./neo-smart-token-specs.component.css']
})
export class NeoSmartTokenSpecsComponent implements OnInit {

  dataSource;
  tokenSpecs = [];
  displayedColumns: Array<string> = [
    "id",
    "name",
    "contract",
    "edit-action",
    "copy-action",
    "remove-action"
  ];

  constructor(
    private neoTokenSpecsService: NeoTokenSpecsService,
    public dialog: MatDialog,
  ) {}

  ngOnInit() {
    this.dataSource = new NeoTokensSpecDataSource(this.neoTokenSpecsService);
  }

  ngAfterViewInit() {
    this.dataSource.tokens$.subscribe(
      (tokenSpecs) => (this.tokenSpecs = tokenSpecs)
    );
  }

  showDialog(tokenSpec) {
    this.dialog.open(NeoSmartTokenSpecsDialogComponent, {
      width: "800px",
      maxHeight: "90vh",
      data: {
        tokenSpec,
      },
    });
  }
}
