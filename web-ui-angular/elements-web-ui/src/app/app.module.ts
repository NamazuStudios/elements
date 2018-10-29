import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { LayoutModule } from '@angular/cdk/layout';
import { FlexLayoutModule } from "@angular/flex-layout";
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { routing } from './app.routing';
import { AuthenticationGuard } from './authentication.guard';
import { AuthenticationInterceptor } from './authentication.interceptor';
import { AppComponent } from './app.component';
import { TopMenuComponent } from './top-menu/top-menu.component';
import { UsersListComponent } from './users-list/users-list.component';
import { ApplicationsListComponent } from './applications-list/applications-list.component';
import { LoginComponent } from './login/login.component';
import { HomeComponent } from './home/home.component';
import { ConfirmationDialogComponent } from './confirmation-dialog/confirmation-dialog.component';

import { AuthenticationService } from "./authentication.service";
import { AlertService } from "./alert.service";
import {ApiErrorInterceptor} from "./api-error.interceptor";
import {MaterialModule} from "./material.module";
import {ConfirmationDialogService} from "./confirmation-dialog/confirmation-dialog.service";
import {ApplicationDialogComponent} from "./application-dialog/application-dialog.component";
import { UserDialogComponent } from './user-dialog/user-dialog.component';

@NgModule({
  entryComponents: [
    ConfirmationDialogComponent,
    ApplicationDialogComponent,
    UserDialogComponent
  ],
  declarations: [
    AppComponent,
    TopMenuComponent,
    UsersListComponent,
    ApplicationsListComponent,
    LoginComponent,
    HomeComponent,
    ConfirmationDialogComponent,
    ApplicationDialogComponent,
    UserDialogComponent
  ],
  imports: [
    BrowserModule,
    FormsModule,
    BrowserAnimationsModule,
    HttpClientModule,
    LayoutModule,
    FlexLayoutModule,
    ReactiveFormsModule,
    MaterialModule,
    routing
  ],
  providers: [
    AuthenticationService,
    AlertService,
    ConfirmationDialogService,
    AuthenticationGuard,
    { provide: HTTP_INTERCEPTORS, useClass: AuthenticationInterceptor, multi: true },
    { provide: HTTP_INTERCEPTORS, useClass: ApiErrorInterceptor, multi: true }
  ],
  bootstrap: [AppComponent]
})

export class AppModule { }
