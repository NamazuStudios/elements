import { AfterViewInit, Component, Input, OnInit, ViewChild } from "@angular/core";
import { MatPaginator } from "@angular/material/paginator";
import { filter, tap } from "rxjs/operators";
import { SelectionModel } from "@angular/cdk/collections";
import { MatDialog } from "@angular/material/dialog";
import { MatTable } from "@angular/material/table";
import { AlertService } from "../alert.service";
import { ConfirmationDialogService } from "../confirmation-dialog/confirmation-dialog.service";

import { UsersService } from "../api/services";
import { User } from "../api/models";
import { NeoTokensService } from "../api/services/blockchain/neo-tokens.service";
import { TransferOptionsPipe } from "./transferOptions.pipe";
import { MintTokenRequest } from "../api/models/blockchain/mint-token-request";
import { NeoSmartContractsService } from "../api/services/blockchain/neo-smart-contracts.service";
import { NeoSmartContractMintDialogComponent } from "../neo-smart-contract-mint-dialog/neo-smart-contract-mint-dialog.component";
import { TokenViewerDialogComponent } from "../token-viewer-dialog/token-viewer-dialog.component";
import { TokenDefinitionsDataSource } from '../token-definitions.datasource';
import { TokenDefinitionService } from '../api/services/blockchain/token-definition.service';
import { TokenDefinition } from '../api/models/blockchain/token-definition';
import { TokenDefinationDuplicateDialogComponent } from '../token-defination-duplicate-dialog/token-defination-duplicate-dialog.component';
import { NeoTokenDialogUpdatedComponent } from '../neo-token-dialog-updated/neo-token-dialog-updated.component';


@Component({
  selector: "app-neo-tokens-list",
  templateUrl: "./neo-tokens-list.component.html",
  styleUrls: ["./neo-tokens-list.component.css"],
  providers: [TransferOptionsPipe]
})
export class NeoTokensListComponent implements OnInit, AfterViewInit {
  @Input() isMinted = false;
  hasSelection = false;
  selection: SelectionModel<TokenDefinition>;
  dataSource: TokenDefinitionsDataSource;
  tokenToCopy;

  displayedColumns: Array<string> = [
    "select",
    "id",
    "name",
    "type",
    "quantity",
    "contract",
    "network",
    "public",
    "listed",
    "transferOptions",
    // "list-action",
    // "view-action",
    "edit-action",
    "copy-action",
    "remove-action",
  ];
  currentTokens: TokenDefinition[];
  currentUser: User;

  @ViewChild(MatPaginator) paginator: MatPaginator;
  // @ViewChild('input') input: ElementRef;
  @ViewChild(MatTable) table: MatTable<TokenDefinition>;

  constructor(
    private tokenDefinitionService: TokenDefinitionService,
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
    this.selection = new SelectionModel<TokenDefinition>(true, []);
    this.dataSource = new TokenDefinitionsDataSource(this.tokenDefinitionService);
    // this.refresh(0);

    this.neoTokensService.network.subscribe(
      () => {
        this.refresh(0);
      }
    );
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
      (currentTokens) => {
        this.currentTokens = currentTokens;
      }
    );
    this.dataSource.totalCount$.subscribe(
      (totalCount) => (this.paginator.length = totalCount)
    );
  }

  addToken() {
    this.showDialog(null);
  }

  // add support for searching here
  refresh(delay = 500) {
    setTimeout(() => {
      this.selection.clear();
      this.dataSource.loadTokens(
        this.paginator.pageIndex * this.paginator.pageSize,
        this.paginator.pageSize,
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
    this.tokenDefinitionService.deleteTokenDefinition(token.id).subscribe(
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

  showDialog(token: TokenDefinition) {
    this.dialog.open(NeoTokenDialogUpdatedComponent, {
      width: "850px",
      data: {
        token,
        refresher: this,
      },
    });
  }

  copyToken(token: TokenDefinition) {
    this.tokenToCopy = token;
    this.dialog.open(TokenDefinationDuplicateDialogComponent, {
      width: "500px",
      data: {
        submit: this.submitCopyToken.bind(this),
      },
    });
  }

  submitCopyToken(name: string) {
    const data = {
      name,
      displayName: name,
      metadataSpecId: this.tokenToCopy.metadataSpec.id,
      contractId: this.tokenToCopy.contract.id,
      userId: this.tokenToCopy.user.id,
      metadata: this.tokenToCopy.metadata,
    }
    this.tokenDefinitionService.createTokenDefinition(data)
      .subscribe(() => {
        this.refresh();
      });
    this.tokenToCopy = null;
  }

  showMintDialog(neoToken: TokenDefinition, next) {
    this.dialog.open(NeoSmartContractMintDialogComponent, {
      width: "500px",
      data: {
        neoToken,
        next: next,
        refresher: this,
      },
    });
  }

  mintToken(token: TokenDefinition){
    this.showMintDialog(
      token,
      (mintTokenRequest: MintTokenRequest) => {
        return this.neoSmartContractService.mintToken(mintTokenRequest);
      }
    );
  }

  // listToken(token: TokenDefinition, listed: boolean) {
  //   this.neoTokensService
  //     .updateToken({
  //       id: token.id,
  //       body: {
  //         token: token.token,
  //         listed,
  //         contractId: token.contractId,
  //       },
  //     })
  //     .subscribe(() => {
  //       this.refresh();
  //     });
  // }

  editToken(token: TokenDefinition) {
    this.showDialog(token);
  }

  viewToken(token: TokenDefinition) {
    this.dialog.open(TokenViewerDialogComponent, {
      width: "600px",
      data: {
        token
      }
    });
  }

  getTransferOptionsToolTip(data){
    return this.transferOptionsPipe.transform(data, 'toolTip');
  }
}
