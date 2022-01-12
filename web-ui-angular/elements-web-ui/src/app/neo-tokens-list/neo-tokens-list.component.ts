import { AfterViewInit, Component, OnInit, ViewChild } from "@angular/core";
import { MatPaginator } from "@angular/material/paginator";
import { filter, tap } from "rxjs/operators";
import { SelectionModel } from "@angular/cdk/collections";
import { MatDialog } from "@angular/material/dialog";
import { MatTable } from "@angular/material/table";
import { AlertService } from "../alert.service";
import { ConfirmationDialogService } from "../confirmation-dialog/confirmation-dialog.service";

import { UsersService } from "../api/services";
import { User } from "../api/models";
import { NeoToken } from "../api/models/blockchain/neo-token";
import { NeoTokensDataSource } from "../neo-tokens.datasource";
import { NeoTokensService } from "../api/services/blockchain/neo-tokens.service";
import { NeoTokenViewModel } from "../models/blockchain/neo-token-view-model";
import { CreateNeoTokenRequest } from "../api/models/blockchain/create-neo-token-request";
import { UpdateNeoTokenRequest } from "../api/models/blockchain/update-neo-token-request";
import {
  NeoTokenDialogComponent, OptionType,
} from "../neo-token-dialog/neo-token-dialog.component";
import { TransferOptionsPipe } from "./transferOptions.pipe";
import { MintTokenRequest } from "../api/models/blockchain/mint-token-request";
import { NeoSmartContractsService } from "../api/services/blockchain/neo-smart-contracts.service";
import { NeoSmartContractMintDialogComponent } from "../neo-smart-contract-mint-dialog/neo-smart-contract-mint-dialog.component";


@Component({
  selector: "app-neo-tokens-list",
  templateUrl: "./neo-tokens-list.component.html",
  styleUrls: ["./neo-tokens-list.component.css"],
  providers: [TransferOptionsPipe]
})
export class NeoTokensListComponent implements OnInit, AfterViewInit {
  hasSelection = false;
  selection: SelectionModel<NeoToken>;
  dataSource: NeoTokensDataSource;

  displayedColumns: Array<string> = [
    "select",
    "name",
    // "type",
    "totalSupply",
    // "spec",
    "network",
    "transferOptions",
    "accessOption",
    "listed",
    "revocable",
    "renewable",
    "mint-action",
    "list-action",
    "view-action",
    "edit-action",
    "copy-action",
    "remove-action",
  ];
  currentTokens: NeoToken[];
  currentUser: User;

  @ViewChild(MatPaginator) paginator: MatPaginator;
  // @ViewChild('input') input: ElementRef;
  @ViewChild(MatTable) table: MatTable<NeoToken>;

  constructor(
    private neoTokensService: NeoTokensService,
    private neoSmartContractService: NeoSmartContractsService,
    private alertService: AlertService,
    private dialogService: ConfirmationDialogService,
    public dialog: MatDialog,
    public transferOptionsPipe: TransferOptionsPipe,
    public userService: UsersService
  ) {}

  ngOnInit() {
    this.userService
      .getUser()
      .subscribe(
        (user: User) => (this.currentUser = JSON.parse(JSON.stringify(user)))
      );
    this.selection = new SelectionModel<NeoToken>(true, []);
    this.dataSource = new NeoTokensDataSource(this.neoTokensService);
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
    this.dataSource.tokens$.subscribe(
      (currentTokens) => (this.currentTokens = currentTokens)
    );
    this.dataSource.totalCount$.subscribe(
      (totalCount) => (this.paginator.length = totalCount)
    );
  }

  // add support for searching here
  refresh(delay = 500) {
    setTimeout(() => {
      this.selection.clear();
      this.dataSource.loadTokens(
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
    const numRows = this.currentTokens.length;
    return numSelected == numRows;
  }

  /** Selects all rows if they are not all selected; otherwise clear selection. */
  masterToggle() {
    this.isAllSelected()
      ? this.selection.clear()
      : this.currentTokens.forEach((row) => this.selection.select(row));
  }

  deleteToken(token) {
    this.dialogService
      .confirm(
        "Confirm Dialog",
        `Are you sure you want to remove the token '${token.displayName}'?`
      )
      .pipe(filter((r) => r))
      .subscribe((res) => {
        this.doDeleteToken(token);
        this.refresh();
      });
  }

  doDeleteToken(token) {
    this.neoTokensService.deleteToken(token.id).subscribe(
      (r) => {},
      (error) => this.alertService.error(error)
    );
  }

  deleteSelectedTokens() {
    this.dialogService
      .confirm(
        "Confirm Dialog",
        `Are you sure you want to remove ${
          this.selection.selected.length
        } selected token${this.selection.selected.length == 1 ? "" : "s"}?`
      )
      .pipe(filter((r) => r))
      .subscribe((res) => {
        this.selection.selected.forEach((row) => this.doDeleteToken(row));
        this.selection.clear();
        this.refresh(500);
      });
  }

  showDialog(isNew: boolean, neoToken: NeoToken, next) {
    this.dialog.open(NeoTokenDialogComponent, {
      width: "850px",
      data: {
        isNew: isNew,
        neoToken,
        next: next,
        refresher: this,
      },
    });
  }

  addToken() {
    let neoTokenViewModel = new NeoTokenViewModel();

    neoTokenViewModel.id = "";
    neoTokenViewModel.token = {
      owner: "",
      name: "",
      description: "",
      tags: [],
      totalSupply: 0,
      accessOption: "private",
      previewUrls: [],
      assetUrls: [],
      ownership: { stakeHolders: [], capitalization: 0 },
      transferOptions: "none",
      revocable: false,
      expiry: 0,
      renewable: false,
      metadata: {},
    };
    neoTokenViewModel.contractId = "";
    neoTokenViewModel.listed = false;
    neoTokenViewModel.minted = false;

    this.showDialog(
      true,
      neoTokenViewModel,
      (createTokenRequest: CreateNeoTokenRequest) => {
        return this.neoTokensService.createToken(createTokenRequest);
      }
    );
  }

  copyToken(token: NeoToken) {
    this.showDialog(
      true,
      token,
      (createTokenRequest: CreateNeoTokenRequest) => {
        return this.neoTokensService.createToken(createTokenRequest);
      }
    );
  }

  showMintDialog(neoToken: NeoToken, next) {
    this.dialog.open(NeoSmartContractMintDialogComponent, {
      width: "850px",
      data: {
        neoToken,
        next: next,
        refresher: this,
      },
    });
  }

  mintToken(token: NeoToken){
    this.showMintDialog(
      token,
      (mintTokenRequest: MintTokenRequest) => {
        return this.neoSmartContractService.mintToken(mintTokenRequest);
      }
    );
  }

  listToken(token: NeoToken) {
    this.neoTokensService
      .updateToken({
        id: token.id,
        body: {
          token: token.token,
          listed: true,
          contractId: token.contractId,
        },
      })
      .subscribe(() => {
        this.refresh();
      });
  }

  editToken(token: NeoToken) {
    this.showDialog(
      false,
      token,
      (updateNeoTokenRequest: UpdateNeoTokenRequest) => {
        return this.neoTokensService.updateToken({
          id: token.id,
          body: updateNeoTokenRequest,
        });
      }
    );
  }

  getTransferOptionsToolTip(data){
    return this.transferOptionsPipe.transform(data, 'toolTip');
  }
}
