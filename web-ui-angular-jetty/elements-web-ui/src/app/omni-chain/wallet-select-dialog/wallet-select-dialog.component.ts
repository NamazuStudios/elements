import {
  AfterViewInit,
  Component,
  ElementRef,
  Inject,
  OnInit,
  ViewChild,
} from "@angular/core";
import { User } from "../../api/models/user";
import {
  MAT_DIALOG_DATA,
  MatDialog,
  MatDialogRef,
} from "@angular/material/dialog";
import { MatPaginator } from "@angular/material/paginator";
import { MatSnackBar } from "@angular/material/snack-bar";
import { MatTable } from "@angular/material/table";
import { UsersService } from "../../api/services/users.service";
import { AlertService } from "../../alert.service";
import { UsersDataSource } from "../../users/users.datasource";
import { fromEvent } from "rxjs";
import { debounceTime, distinctUntilChanged, tap } from "rxjs/operators";
import { NeoSmartContractsDataSource } from "../neo/neo-smart-contracts.datasource";
import { NeoSmartContractsService } from "../../api/services/blockchain/neo-smart-contracts.service";
import { NeoSmartContract } from "../../api/models/blockchain/neo-smart-contract";
import { NeoSmartContractsListComponent } from "../neo/neo-smart-contracts-list/neo-smart-contracts-list.component";
import { NeoWalletsDataSource } from "../neo/neo-wallets.datasource";
import { NeoWallet } from "../../api/models/blockchain/neo-wallet";
import { NeoWalletsService } from "../../api/services";

@Component({
  selector: 'app-wallet-select-dialog',
  templateUrl: './wallet-select-dialog.component.html',
  styleUrls: ['./wallet-select-dialog.component.css']
})
export class WalletSelectDialogComponent implements OnInit, AfterViewInit {

  dataSource: NeoWalletsDataSource;
  displayedColumns = ["displayName", "username",  "actions"];
  currentNeoWallets: NeoWallet[];

  @ViewChild(MatPaginator) paginator: MatPaginator;
  @ViewChild("input") input: ElementRef;
  @ViewChild(MatTable) table: MatTable<NeoWallet>;

  constructor(
    public dialogRef: MatDialogRef<WalletSelectDialogComponent>,
    public dialog: MatDialog,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private neoWalletsService: NeoWalletsService,
    private alertService: AlertService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit() {
    this.dataSource = new NeoWalletsDataSource(this.neoWalletsService);
    this.refresh(0);
  }

  ngAfterViewInit() {
    this.paginator.pageSize = 10;
    // server-side search
    fromEvent(this.input.nativeElement, "keyup")
      .pipe(
        debounceTime(150),
        distinctUntilChanged(),
        tap(() => {
          this.paginator.pageIndex = 0;
          this.refresh();
        })
      )
      .subscribe();

    this.paginator.page.pipe(tap(() => this.refresh())).subscribe();

    this.dataSource.wallets$.subscribe(
      (currentNeoWallets) => (this.currentNeoWallets = currentNeoWallets)
    );
    this.dataSource.totalCount$.subscribe(
      (totalCount) => (this.paginator.length = totalCount)
    );
  }

  refresh(delay = 500) {
    setTimeout(() => {
      this.dataSource.loadWallets(
        this.paginator.pageIndex * this.paginator.pageSize,
        this.paginator.pageSize,
        null,
        "None"
      );
    }, delay);
  }

  close(neoWallet?: NeoWallet) {
    if (!neoWallet) {
      this.dialogRef.close();
      return;
    }

    this.data.next(neoWallet);
    this.dialogRef.close();
  }
}

