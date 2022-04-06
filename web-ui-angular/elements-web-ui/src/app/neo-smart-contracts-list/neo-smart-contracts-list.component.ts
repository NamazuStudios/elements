import {
  AfterViewInit,
  Component,
  ElementRef,
  OnInit,
  ViewChild,
} from "@angular/core";
import { MatPaginator } from "@angular/material/paginator";
import {
  debounceTime,
  distinctUntilChanged,
  filter,
  tap,
} from "rxjs/operators";
import { SelectionModel } from "@angular/cdk/collections";
import { MatDialog } from "@angular/material/dialog";
import { MatTable } from "@angular/material/table";
import { AlertService } from "../alert.service";
import { ConfirmationDialogService } from "../confirmation-dialog/confirmation-dialog.service";

import { UsersService } from "../api/services";
import { User } from "../api/models";
import { NeoSmartContract } from "../api/models/blockchain/neo-smart-contract";
import { NeoSmartContractsService } from "../api/services/blockchain/neo-smart-contracts.service";
import { NeoSmartContractsDataSource } from "../neo-smart-contracts.datasource";
import { PatchNeoSmartContractRequest } from "../api/models/blockchain/patch-neo-smart-contract-request";
import { NeoSmartContractViewModel } from "../models/blockchain/neo-smart-contract-view-model";
import { NeoSmartContractsDialogComponent } from "../neo-smart-contracts-dialog/neo-smart-contracts-dialog.component";
import { fromEvent } from "rxjs";
import { NeoTokenDialogComponent } from "../neo-token-dialog/neo-token-dialog.component";
import { NeoTokenViewModel } from "../models/blockchain/neo-token-view-model";
import { CreateNeoTokenRequest } from "../api/models/blockchain/create-neo-token-request";
import { NeoToken } from "../api/models/blockchain/neo-token";
import { NeoTokensService } from "../api/services/blockchain/neo-tokens.service";

@Component({
  selector: "app-neo-smart-contracts-list",
  templateUrl: "./neo-smart-contracts-list.component.html",
  styleUrls: ["./neo-smart-contracts-list.component.css"],
})
export class NeoSmartContractsListComponent implements OnInit, AfterViewInit {
  hasSelection = false;
  selection: SelectionModel<NeoSmartContract>;
  dataSource: NeoSmartContractsDataSource;

  displayedColumns: Array<string> = [
    "select",
    "name",
    "network",
    "edit-action",
    "define-nft-action",
    "manifest-action",
    "remove-action",
  ];
  currentSmartContracts: NeoSmartContract[];
  currentUser: User;

  @ViewChild(MatPaginator) paginator: MatPaginator;
  @ViewChild("input") input: ElementRef;
  @ViewChild(MatTable) table: MatTable<NeoSmartContract>;

  constructor(
    private neoSmartContractsService: NeoSmartContractsService,
    private alertService: AlertService,
    private dialogService: ConfirmationDialogService,
    public dialog: MatDialog,
    public neoTokensService: NeoTokensService,
    public userService: UsersService
  ) {}

  ngOnInit() {
    this.userService
      .getUser()
      .subscribe(
        (user: User) => (this.currentUser = JSON.parse(JSON.stringify(user)))
      );
    this.selection = new SelectionModel<NeoSmartContract>(true, []);
    this.dataSource = new NeoSmartContractsDataSource(
      this.neoSmartContractsService
    );
    // this.refresh(0);

    this.neoSmartContractsService.network.subscribe(
      () => {
        this.refresh(0);
      }
    );
  }

  ngAfterViewInit() {
    this.paginator.pageSize = 10;
    //server-side search
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

    this.selection.changed.subscribe(
      (s) => (this.hasSelection = this.selection.hasValue())
    );
    this.dataSource.neoSmartContracts$.subscribe(
      (currentSmartContracts) =>
        (this.currentSmartContracts = currentSmartContracts)
    );
    this.dataSource.totalCount$.subscribe(
      (totalCount) => (this.paginator.length = totalCount)
    );
  }

  // add support for searching here
  refresh(delay = 500) {
    setTimeout(() => {
      this.selection.clear();
      this.dataSource.loadNeoSmartContracts(
        this.paginator.pageIndex * this.paginator.pageSize,
        this.paginator.pageSize,
        this.input.nativeElement.value // for searching...
        //"None"
      );
    }, delay);
  }

  /** Whether the number of selected elements matches the total number of rows. */
  isAllSelected() {
    const numSelected = this.selection.selected.length;
    const numRows = this.currentSmartContracts.length;
    return numSelected == numRows;
  }

  /** Selects all rows if they are not all selected; otherwise clear selection. */
  masterToggle() {
    this.isAllSelected()
      ? this.selection.clear()
      : this.currentSmartContracts.forEach((row) => this.selection.select(row));
  }

  deleteNeoSmartContract(neoSmartContract) {
    this.dialogService
      .confirm(
        "Confirm Dialog",
        `Are you sure you want to remove the neoSmartContract '${neoSmartContract.displayName}'?`
      )
      .pipe(filter((r) => r))
      .subscribe((res) => {
        this.doDeleteNeoSmartContract(neoSmartContract);
        this.refresh();
      });
  }

  doDeleteNeoSmartContract(neoSmartContract) {
    this.neoSmartContractsService
      .deleteNeoSmartContract(neoSmartContract.id)
      .subscribe(
        (r) => {},
        (error) => this.alertService.error(error)
      );
  }

  deleteSelectedNeoSmartContracts() {
    this.dialogService
      .confirm(
        "Confirm Dialog",
        `Are you sure you want to remove ${
          this.selection.selected.length
        } selected Neo Smart Contract${
          this.selection.selected.length == 1 ? "" : "s"
        }?`
      )
      .pipe(filter((r) => r))
      .subscribe((res) => {
        this.selection.selected.forEach((row) =>
          this.doDeleteNeoSmartContract(row)
        );
        this.selection.clear();
        this.refresh(500);
      });
  }

  showDialog(isNew: boolean, neoSmartContract: NeoSmartContract, next) {
    let neoSmartContractViewModel: NeoSmartContractViewModel = neoSmartContract;

    this.dialog.open(NeoSmartContractsDialogComponent, {
      width: "700px",
      data: {
        isNew: isNew,
        neoSmartContract: neoSmartContractViewModel,
        next: next,
        refresher: this,
      },
    });
  }

  addNeoSmartContract() {

    let neoSmartContractViewModel = new NeoSmartContractViewModel();

    neoSmartContractViewModel.blockchain = "";
    neoSmartContractViewModel.displayName = "";
    neoSmartContractViewModel.metadata = {};
    neoSmartContractViewModel.scriptHash = "";

    this.showDialog(
      true,
      neoSmartContractViewModel,
      (patchNeoSmartContractRequest: PatchNeoSmartContractRequest) => {
        return this.neoSmartContractsService.patchNeoSmartContract(
          patchNeoSmartContractRequest
        );
      }
    );
  }

  editNeoSmartContract(neoSmartContract: NeoSmartContract) {
    this.showDialog(
      false,
      neoSmartContract,
      (patchNeoSmartContractRequest: PatchNeoSmartContractRequest) => {
        return this.neoSmartContractsService.patchNeoSmartContract(
          patchNeoSmartContractRequest
        );
      }
    );
  }

  //TODO: refactor this code out...
  showTokenDialog(isNew: boolean, neoToken: NeoToken, next) {
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

  public addToken(neoSmartContract: NeoSmartContract) {
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
    neoTokenViewModel.contractId = neoSmartContract.id;
    neoTokenViewModel.listed = false;
    neoTokenViewModel.minted = false;

    this.showTokenDialog(
      true,
      neoTokenViewModel,
      (createTokenRequest: CreateNeoTokenRequest) => {
        return this.neoTokensService.createToken(createTokenRequest);
      }
    );
  }

}
