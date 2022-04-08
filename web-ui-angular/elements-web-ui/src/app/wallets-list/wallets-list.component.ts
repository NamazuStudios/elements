import { AfterViewInit, Component, OnInit, ViewChild } from "@angular/core";
import { MatPaginator } from "@angular/material/paginator";
import { filter, tap } from "rxjs/operators";
import { SelectionModel } from "@angular/cdk/collections";
import { MatDialog } from "@angular/material/dialog";
import { MatTable } from "@angular/material/table";
import { AlertService } from "../alert.service";
import { ConfirmationDialogService } from "../confirmation-dialog/confirmation-dialog.service";

import { WalletDialogComponent } from "../wallets-dialog/wallet-dialog.component";
import { NeoWallet } from "../api/models/blockchain/neo-wallet";
import { NeoWalletsDataSource } from "../neo-wallets.datasource";
import { NeoWalletsService, UsersService } from "../api/services";
import { User } from "../api/models";
import { NeoWalletViewModel } from "../models/blockchain/neo-wallet-view-model";
import { CreateNeoWalletRequest } from "../api/models/blockchain/create-neo-wallet-request";
import { UpdateNeoWalletRequest } from "../api/models/blockchain/update-neo-wallet-request";

@Component({
  selector: "app-wallets-list",
  templateUrl: "./wallets-list.component.html",
  styleUrls: ["./wallets-list.component.css"],
})
export class WalletsListComponent implements OnInit, AfterViewInit {
  hasSelection = false;
  selection: SelectionModel<NeoWallet>;
  dataSource: NeoWalletsDataSource;

  displayedColumns: Array<string> = [
    "select",
    "owner",
    "name",
    "level",
    "network",
    "content",
    //"active",
    "content-action",
    "edit-action",
    "remove-action",
  ];
  currentWallets: NeoWallet[];
  currentUser: User;

  @ViewChild(MatPaginator) paginator: MatPaginator;
  // @ViewChild('input') input: ElementRef;
  @ViewChild(MatTable) table: MatTable<NeoWallet>;

  constructor(
    private neoWalletsService: NeoWalletsService,
    private alertService: AlertService,
    private dialogService: ConfirmationDialogService,
    public dialog: MatDialog,
    public userService: UsersService
  ) {}

  ngOnInit() {
    this.userService
      .getUser()
      .subscribe(
        (user: User) => (this.currentUser = JSON.parse(JSON.stringify(user)))
      );
    this.selection = new SelectionModel<NeoWallet>(true, []);
    this.dataSource = new NeoWalletsDataSource(this.neoWalletsService);
    this.refresh(0);
  }

  ngAfterViewInit() {
    this.paginator.pageSize = 10;
    // server-side search
    // fromEvent(this.input.nativeElement,'keyup')
    //   .pipe(
    //     debounceTime(150),
    //     distinctUntilChanged(),
    //     tap(() => {
    //       this.paginator.pageIndex = 0;
    //       this.refresh();
    //     })
    //   )
    //   .subscribe();

    this.paginator.page.pipe(tap(() => this.refresh())).subscribe();

    this.selection.changed.subscribe(
      (s) => (this.hasSelection = this.selection.hasValue())
    );
    this.dataSource.wallets$.subscribe(
      (currentWallets) => (this.currentWallets = currentWallets)
    );
    this.dataSource.totalCount$.subscribe(
      (totalCount) => (this.paginator.length = totalCount)
    );
  }

  // add support for searching here
  refresh(delay = 500) {
    setTimeout(() => {
      this.selection.clear();
      this.dataSource.loadWallets(
        this.paginator.pageIndex * this.paginator.pageSize,
        this.paginator.pageSize,
        //TODO: We'll need to switch these to YOURS | SUPERUSERS | USERS
        null,
        //this.input.nativeElement.value, // for searching...
        "None"
      );
    }, delay);
  }

  /** Whether the number of selected elements matches the total number of rows. */
  isAllSelected() {
    const numSelected = this.selection.selected.length;
    const numRows = this.currentWallets.length;
    return numSelected == numRows;
  }

  /** Selects all rows if they are not all selected; otherwise clear selection. */
  masterToggle() {
    this.isAllSelected()
      ? this.selection.clear()
      : this.currentWallets.forEach((row) => this.selection.select(row));
  }

  deleteWallet(wallet) {
    this.dialogService
      .confirm(
        "Confirm Dialog",
        `Are you sure you want to remove the wallet '${wallet.displayName}'?`
      )
      .pipe(filter((r) => r))
      .subscribe((res) => {
        this.doDeleteWallet(wallet);
        this.refresh();
      });
  }

  doDeleteWallet(wallet) {
    this.neoWalletsService.deleteWallet(wallet.id).subscribe(
      (r) => {},
      (error) => this.alertService.error(error)
    );
  }

  deleteSelectedWallets() {
    this.dialogService
      .confirm(
        "Confirm Dialog",
        `Are you sure you want to remove ${
          this.selection.selected.length
        } selected wallet${this.selection.selected.length == 1 ? "" : "s"}?`
      )
      .pipe(filter((r) => r))
      .subscribe((res) => {
        this.selection.selected.forEach((row) => this.doDeleteWallet(row));
        this.selection.clear();
        this.refresh(500);
      });
  }

  showDialog(isNew: boolean, neoWallet: NeoWallet, next) {
    let neoWalletViewModel: NeoWalletViewModel = neoWallet;

    this.dialog.open(WalletDialogComponent, {
      width: "700px",
      data: {
        isNew: isNew,
        wallet: neoWalletViewModel,
        next: next,
        refresher: this,
      },
    });
  }

  addWallet() {
    this.showDialog(
      true,
      new NeoWalletViewModel(),
      (createWalletRequest: CreateNeoWalletRequest) => {
        return this.neoWalletsService.createWallet(createWalletRequest);
      }
    );
  }

  editWallet(wallet: NeoWallet) {
    this.showDialog(
      false,
      wallet,
      (updateNeoWalletRequest: UpdateNeoWalletRequest) => {
        return this.neoWalletsService.updateWallet({
          id: updateNeoWalletRequest.walletId,
          body: updateNeoWalletRequest,
        });
      }
    );
  }
}
