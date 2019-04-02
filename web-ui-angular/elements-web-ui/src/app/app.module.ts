import { BrowserModule } from '@angular/platform-browser';
import { APP_INITIALIZER, NgModule} from '@angular/core';
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
import {MaterialModule} from "./material/material.module";
import {ConfirmationDialogService} from "./confirmation-dialog/confirmation-dialog.service";
import {ApplicationDialogComponent} from "./application-dialog/application-dialog.component";
import { UserDialogComponent } from './user-dialog/user-dialog.component';
import { ApplicationConfigurationsListComponent } from './application-configurations-list/application-configurations-list.component';
import { FacebookApplicationConfigurationDialogComponent } from './facebook-application-configuration-dialog/facebook-application-configuration-dialog.component';
import { FirebaseApplicationConfigurationDialogComponent } from './firebase-application-configuration-dialog/firebase-application-configuration-dialog.component';
import {ConfigService} from "./config.service";
import { MatchmakingApplicationConfigurationDialogComponent } from './matchmaking-application-configuration-dialog/matchmaking-application-configuration-dialog.component';
import { GameOnApplicationConfigurationDialogComponent } from './game-on-application-configuration-dialog/game-on-application-configuration-dialog.component';
import { GameOnPrizeDialogComponent } from './game-on-prize-dialog/game-on-prize-dialog.component';
import { GameOnPrizesListComponent } from './game-on-prizes-list/game-on-prizes-list.component';
import { ItemsListComponent } from './items-list/items-list.component';
import { ItemDialogComponent } from './item-dialog/item-dialog.component';
import { SimpleJsonEditorComponent } from './json-editor-card/simple-json-editor/simple-json-editor.component';
import {NgJsonEditorModule} from 'ang-jsoneditor';
import { JsonEditorCardComponent } from './json-editor-card/json-editor-card.component';
import { MissionsListComponent } from './missions-list/missions-list.component';
import { MissionDialogComponent } from './mission-dialog/mission-dialog.component';
import { MissionStepsCardComponent } from './mission-dialog/mission-steps-card/mission-steps-card.component';
import { DragDropModule } from '@angular/cdk/drag-drop';
import { MissionRewardsEditorComponent } from './mission-dialog/mission-rewards-editor/mission-rewards-editor.component';
import { IosApplicationConfigurationDialogComponent } from './ios-application-configuration-dialog/ios-application-configuration-dialog.component';
import { AndroidGooglePlayConfigurationDialogComponent } from './android-google-play-configuration-dialog/android-google-play-configuration-dialog.component';
import { ProductBundleListComponent } from './product-bundle-list/product-bundle-list.component';
import { ProductBundleEditorComponent } from './product-bundle-editor/product-bundle-editor.component';

export function initialize(configService: ConfigService) {
  return () => configService.load();
}

@NgModule({
  entryComponents: [
    ConfirmationDialogComponent,
    ApplicationDialogComponent,
    UserDialogComponent,
    ItemDialogComponent,
    MissionDialogComponent,
    FacebookApplicationConfigurationDialogComponent,
    FirebaseApplicationConfigurationDialogComponent,
    MatchmakingApplicationConfigurationDialogComponent,
    GameOnApplicationConfigurationDialogComponent,
    GameOnPrizeDialogComponent,
    IosApplicationConfigurationDialogComponent,
    AndroidGooglePlayConfigurationDialogComponent,
    ProductBundleEditorComponent
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
    ItemDialogComponent,
    UserDialogComponent,
    ApplicationConfigurationsListComponent,
    FacebookApplicationConfigurationDialogComponent,
    FirebaseApplicationConfigurationDialogComponent,
    MatchmakingApplicationConfigurationDialogComponent,
    GameOnApplicationConfigurationDialogComponent,
    GameOnPrizesListComponent,
    GameOnPrizeDialogComponent,
    ItemsListComponent,
    ItemDialogComponent,
    SimpleJsonEditorComponent,
    JsonEditorCardComponent,
    MissionsListComponent,
    MissionDialogComponent,
    MissionStepsCardComponent,
    MissionRewardsEditorComponent,
    IosApplicationConfigurationDialogComponent,
    AndroidGooglePlayConfigurationDialogComponent,
    ProductBundleListComponent,
    ProductBundleEditorComponent,
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
    routing,
    NgJsonEditorModule,
    DragDropModule
  ],
  providers: [
    ConfigService,
    AuthenticationService,
    AlertService,
    ConfirmationDialogService,
    AuthenticationGuard,
    { provide: APP_INITIALIZER, useFactory: initialize, multi: true, deps: [ ConfigService ] },
    { provide: HTTP_INTERCEPTORS, useClass: AuthenticationInterceptor, multi: true },
    { provide: HTTP_INTERCEPTORS, useClass: ApiErrorInterceptor, multi: true },
  ],
  bootstrap: [AppComponent]
})

export class AppModule { }
