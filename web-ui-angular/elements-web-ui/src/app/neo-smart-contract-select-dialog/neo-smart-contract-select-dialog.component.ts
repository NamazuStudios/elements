import {
  AfterViewInit,
  Component,
  ElementRef,
  Inject,
  OnInit,
  ViewChild,
} from "@angular/core";
import { User } from "../api/models/user";
import {
  MAT_DIALOG_DATA,
  MatDialog,
  MatDialogRef,
} from "@angular/material/dialog";
import { MatPaginator } from "@angular/material/paginator";
import { MatSnackBar } from "@angular/material/snack-bar";
import { MatTable } from "@angular/material/table";
import { UsersService } from "../api/services/users.service";
import { AlertService } from "../alert.service";
import { UsersDataSource } from "../users.datasource";
import { fromEvent } from "rxjs";
import { debounceTime, distinctUntilChanged, tap } from "rxjs/operators";
import { NeoSmartContractsDataSource } from "../neo-smart-contracts.datasource";
import { NeoSmartContractsService } from "../api/services/blockchain/neo-smart-contracts.service";
import { NeoSmartContract } from "../api/models/blockchain/neo-smart-contract";
import { NeoSmartContractsListComponent } from "../neo-smart-contracts-list/neo-smart-contracts-list.component";

@Component({
  selector: "app-neo-smart-contract-select-dialog",
  templateUrl: "./neo-smart-contract-select-dialog.component.html",
  styleUrls: ["./neo-smart-contract-select-dialog.component.css"],
})
export class NeoSmartContractSelectDialogComponent
  implements OnInit, AfterViewInit
{
  dataSource: NeoSmartContractsDataSource;
  displayedColumns = ["displayName", "blockchain",  "actions"];
  currentNeoSmartContracts: NeoSmartContract[];

  @ViewChild(MatPaginator) paginator: MatPaginator;
  @ViewChild("input") input: ElementRef;
  @ViewChild(MatTable) table: MatTable<NeoSmartContract>;

  constructor(
    public dialogRef: MatDialogRef<NeoSmartContractSelectDialogComponent>,
    public dialog: MatDialog,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private neoSmartContractsService: NeoSmartContractsService,
    private alertService: AlertService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit() {
    this.dataSource = new NeoSmartContractsDataSource(this.neoSmartContractsService);
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

    this.dataSource.neoSmartContracts$.subscribe(
      (currentNeoSmartContracts) => (this.currentNeoSmartContracts = currentNeoSmartContracts)
    );
    this.dataSource.totalCount$.subscribe(
      (totalCount) => (this.paginator.length = totalCount)
    );
  }

  refresh(delay = 500) {
    setTimeout(() => {
      this.dataSource.loadNeoSmartContracts(
        this.paginator.pageIndex * this.paginator.pageSize,
        this.paginator.pageSize,
        this.input.nativeElement.value,
      );
    }, delay);
  }

  close(neoSmartContract?: NeoSmartContract) {
    if (!neoSmartContract) {
      this.dialogRef.close();
      return;
    }

    this.data.next(neoSmartContract);
    this.dialogRef.close();
  }
}
