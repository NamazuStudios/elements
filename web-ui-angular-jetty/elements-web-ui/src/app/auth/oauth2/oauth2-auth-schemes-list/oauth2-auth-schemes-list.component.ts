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
import { Oauth2AuthSchemesDatasource } from "../oauth2-auth-schemes.datasource";
import { UpdateOauth2AuthSchemeRequest } from "../../../api/models/auth/update-oauth2-auth-scheme-request";
import { CreateOauth2AuthSchemeRequest } from "../../../api/models/auth/create-oauth2-auth-scheme-request";
import { Oauth2AuthSchemeDialogComponent } from "../oauth2-auth-scheme-dialog/oauth2-auth-scheme-dialog.component";
import { Clipboard } from "@angular/cdk/clipboard";
import { MatSnackBar } from "@angular/material/snack-bar";
import {Oauth2AuthSchemeViewModel} from "../../../models/oauth2-auth-scheme-view-model";
import {Oauth2AuthScheme} from "../../../api/models/auth/auth-scheme-oauth2";
import {Oauth2AuthSchemesService} from "../../../api/services/auth/oauth2-auth-schemes.service";

@Component({
  selector: "oauth2-app-auth-schemes-list",
  templateUrl: "./oauth2-auth-schemes-list.component.html",
  styleUrls: ["./oauth2-auth-schemes-list.component.css"],
})
export class Oauth2AuthSchemesListComponent implements OnInit, AfterViewInit {
  hasSelection = false;
  selection: SelectionModel<Oauth2AuthScheme>;
  dataSource: Oauth2AuthSchemesDatasource;

  displayedColumns: Array<string> = [
    "select",
    "name",
    "validationUrl",
    "edit-action",
    "remove-action",
  ];
  currentAuthSchemes: Oauth2AuthScheme[];
  currentUser: User;

  @ViewChild(MatPaginator) paginator: MatPaginator;
  // @ViewChild('input') input: ElementRef;
  @ViewChild(MatTable) table: MatTable<Oauth2AuthScheme>;

  constructor(
    private authSchemesService: Oauth2AuthSchemesService,
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
    this.selection = new SelectionModel<Oauth2AuthScheme>(true, []);
    this.dataSource = new Oauth2AuthSchemesDatasource(this.authSchemesService);
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

  deleteAuthScheme(authScheme: Oauth2AuthScheme) {
    this.dialogService
      .confirm(
        "Confirm Dialog",
        `Are you sure you want to remove the authScheme '${authScheme.name}'?`
      )
      .pipe(filter((r) => r))
      .subscribe((res) => {
        this.doDeleteAuthScheme(authScheme);
        this.refresh();
      });
  }

  doDeleteAuthScheme(authScheme: Oauth2AuthScheme) {
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

  showDialog(isNew: boolean, authScheme: Oauth2AuthScheme, next) {
    this.dialog.open(Oauth2AuthSchemeDialogComponent, {
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
    let authSchemeViewModel = new Oauth2AuthSchemeViewModel();
     authSchemeViewModel.id = "";
     authSchemeViewModel.name = "";
     authSchemeViewModel.headers = [];
     authSchemeViewModel.params = [];
     authSchemeViewModel.validationUrl = "";
     authSchemeViewModel.responseIdMapping = ""

    this.showDialog(
      true,
      authSchemeViewModel,
      (createAuthSchemeRequest: CreateOauth2AuthSchemeRequest) => {
        return this.authSchemesService.createAuthScheme(createAuthSchemeRequest);
      }
    );
  }

  editAuthScheme(authScheme: Oauth2AuthScheme) {
    this.showDialog(
      false,
      authScheme,
      (updateAuthSchemeRequest: UpdateOauth2AuthSchemeRequest) => {
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
