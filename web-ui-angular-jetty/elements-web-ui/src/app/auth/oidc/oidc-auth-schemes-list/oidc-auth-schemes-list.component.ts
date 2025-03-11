import { AfterViewInit, Component, OnInit, ViewChild } from "@angular/core";
import { MatPaginator } from "@angular/material/paginator";
import { filter, tap } from "rxjs/operators";
import { SelectionModel } from "@angular/cdk/collections";
import { MatDialog } from "@angular/material/dialog";
import { MatTable } from "@angular/material/table";
import { AlertService } from "../../../alert.service";
import { ConfirmationDialogService } from "../../../confirmation-dialog/confirmation-dialog.service";

import { UsersService } from "../../../api/services";
import { User } from "../../../api/models";
import { OidcAuthSchemesDatasource } from "../oidc-auth-schemes.datasource";
import { OidcAuthSchemesService } from "../../../api/services/auth/oidc-auth-schemes.service";
import { UpdateOidcAuthSchemeRequest } from "../../../api/models/auth/update-oidc-auth-scheme-request";
import { CreateOidcAuthSchemeRequest } from "../../../api/models/auth/create-oidc-auth-scheme-request";
import { OidcAuthSchemeDialogComponent } from "../oidc-auth-scheme-dialog/oidc-auth-scheme-dialog.component";
import { Clipboard } from "@angular/cdk/clipboard";
import { MatSnackBar } from "@angular/material/snack-bar";
import {OidcAuthScheme} from "../../../api/models/auth/auth-scheme-oidc";
import {OidcAuthSchemeViewModel} from "../../../models/oidc-auth-scheme-view-model";

@Component({
  selector: "oidc-app-auth-schemes-list",
  templateUrl: "./oidc-auth-schemes-list.component.html",
  styleUrls: ["./oidc-auth-schemes-list.component.css"],
})
export class OidcAuthSchemesListComponent implements OnInit, AfterViewInit {
  hasSelection = false;
  selection: SelectionModel<OidcAuthScheme>;
  dataSource: OidcAuthSchemesDatasource;

  displayedColumns: Array<string> = [
    "select",
    "issuer",
    "edit-action",
    "remove-action",
  ];
  currentAuthSchemes: OidcAuthScheme[];
  currentUser: User;

  @ViewChild(MatPaginator) paginator: MatPaginator;
  @ViewChild(MatTable) table: MatTable<OidcAuthScheme>;

  constructor(
    private authSchemesService: OidcAuthSchemesService,
    private alertService: AlertService,
    private dialogService: ConfirmationDialogService,
    public dialog: MatDialog,
    private clipboard: Clipboard,
    private snackBar: MatSnackBar,
    public userService: UsersService
  ) {}

  ngOnInit() {
    this.userService
      .getUser()
      .subscribe(
        (user: User) => (this.currentUser = JSON.parse(JSON.stringify(user)))
      );
    this.selection = new SelectionModel<OidcAuthScheme>(true, []);
    this.dataSource = new OidcAuthSchemesDatasource(this.authSchemesService);
    this.refresh(0);
  }

  ngAfterViewInit() {
    this.paginator.pageSize = 10;
    this.paginator.page.pipe(tap(() => this.refresh())).subscribe();
    this.selection.changed.subscribe(
      (s) => (this.hasSelection = this.selection.hasValue())
    );
    this.dataSource.authSchemes$.subscribe(
      (currentAuthSchemes) => (this.currentAuthSchemes = currentAuthSchemes)
    );
    this.dataSource.totalCount$.subscribe(
      (totalCount) => (this.paginator.length = totalCount)
    );
  }

  // add support for searching here
  refresh(delay = 500) {
    setTimeout(() => {
      this.selection.clear();
      this.dataSource.loadAuthSchemes(
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
    const numRows = this.currentAuthSchemes.length;
    return numSelected == numRows;
  }

  /** Selects all rows if they are not all selected; otherwise clear selection. */
  masterToggle() {
    this.isAllSelected()
      ? this.selection.clear()
      : this.currentAuthSchemes.forEach((row) => this.selection.select(row));
  }

  deleteAuthScheme(authScheme) {
    this.dialogService
      .confirm(
        "Confirm Dialog",
        `Are you sure you want to remove the authScheme '${authScheme.audience}'?`
      )
      .pipe(filter((r) => r))
      .subscribe((res) => {
        this.doDeleteAuthScheme(authScheme);
        this.refresh();
      });
  }

  doDeleteAuthScheme(authScheme) {
    this.authSchemesService.deleteAuthScheme(authScheme.id).subscribe(
      (r) => {},
      (error) => this.alertService.error(error)
    );
  }

  deleteSelectedAuthSchemes() {
    this.dialogService
      .confirm(
        "Confirm Dialog",
        `Are you sure you want to remove ${
          this.selection.selected.length
        } selected auth scheme${
          this.selection.selected.length == 1 ? "" : "s"
        }?`
      )
      .pipe(filter((r) => r))
      .subscribe((res) => {
        this.selection.selected.forEach((row) => this.doDeleteAuthScheme(row));
        this.selection.clear();
        this.refresh(500);
      });
  }

  showDialog(isNew: boolean, authScheme: OidcAuthScheme, next) {
    this.dialog.open(OidcAuthSchemeDialogComponent, {
      width: "800px",
      data: {
        isNew: isNew,
        authScheme,
        next: next,
        refresher: this,
      },
    });
  }

  addAuthScheme() {
    let authSchemeViewModel = new OidcAuthSchemeViewModel();
     authSchemeViewModel.id = "";
     authSchemeViewModel.keys = [];
     authSchemeViewModel.issuer = "";
     authSchemeViewModel.keysUrl = "";
     authSchemeViewModel.mediaType = "application/json";

    this.showDialog(
      true,
      authSchemeViewModel,
      (createTokenRequest: CreateOidcAuthSchemeRequest) => {
        return this.authSchemesService.createAuthScheme(createTokenRequest);
      }
    );
  }

  editAuthScheme(authScheme: OidcAuthScheme) {
    this.showDialog(
      false,
      authScheme,
      (updateAuthSchemeRequest: UpdateOidcAuthSchemeRequest) => {
        return this.authSchemesService.updateAuthScheme({
          id: authScheme.id,
          body: updateAuthSchemeRequest,
        });

      }
    );
  }

  copyKeyToClipboard(data: string) {
    this.clipboard.copy(data);
    this.snackBar.open("Key has been copied to clipboard.", "Dismiss", {
      duration: 3000,
    });
  }

}
