import { BrowserModule } from '@angular/platform-browser';
import { APP_INITIALIZER, NgModule} from '@angular/core';
import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { LayoutModule } from '@angular/cdk/layout';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import {MatDialogModule} from '@angular/material/dialog';
import {MatFormFieldModule} from '@angular/material/form-field';

import { routing } from './app.routing';
import { AuthenticationGuard } from './login/authentication.guard';
import { AuthenticationInterceptor } from './login/authentication.interceptor';
import { AppComponent } from './app.component';
import { TopMenuComponent } from './top-menu/top-menu.component';
import { UsersListComponent } from './users/users-list/users-list.component';
import { ApplicationsListComponent } from './applications/applications-list/applications-list.component';
import { LoginComponent } from './login/login.component';
import { HomeComponent } from './home/home.component';
import { ConfirmationDialogComponent } from './confirmation-dialog/confirmation-dialog.component';

import { AuthenticationService } from './login/authentication.service';
import { AlertService } from './alert.service';
import {ApiErrorInterceptor} from './api-error.interceptor';
import {MaterialModule} from './material/material.module';
import {ConfirmationDialogService} from './confirmation-dialog/confirmation-dialog.service';
import {ApplicationDialogComponent} from './applications/application-dialog/application-dialog.component';
import { UserDialogComponent } from './users/user-dialog/user-dialog.component';
import { ApplicationConfigurationsListComponent } from './applications/application-configurations-list/application-configurations-list.component';
import { FacebookApplicationConfigurationDialogComponent } from './applications/facebook-application-configuration-dialog/facebook-application-configuration-dialog.component';
import { FirebaseApplicationConfigurationDialogComponent } from './applications/firebase-application-configuration-dialog/firebase-application-configuration-dialog.component';
import {ConfigService} from './config.service';
import { MatchmakingApplicationConfigurationDialogComponent } from './applications/matchmaking-application-configuration-dialog/matchmaking-application-configuration-dialog.component';
import { ItemsListComponent } from './digital-goods/items-list/items-list.component';
import { ItemDialogComponent } from './digital-goods/item-dialog/item-dialog.component';
import { SimpleJsonEditorComponent } from './json-editor-card/simple-json-editor/simple-json-editor.component';
import {NgJsonEditorModule} from 'ang-jsoneditor';
import { JsonEditorCardComponent } from './json-editor-card/json-editor-card.component';
import { MissionsListComponent } from './missions/missions-list/missions-list.component';
import { MissionDialogComponent } from './missions/mission-dialog/mission-dialog.component';
import { MissionStepsCardComponent } from './missions/mission-dialog/mission-steps-card/mission-steps-card.component';
import { DragDropModule } from '@angular/cdk/drag-drop';
import { MissionRewardsEditorComponent } from './missions/mission-dialog/mission-rewards-editor/mission-rewards-editor.component';
import { IosApplicationConfigurationDialogComponent } from './applications/ios-application-configuration-dialog/ios-application-configuration-dialog.component';
import { AndroidGooglePlayConfigurationDialogComponent } from './applications/android-google-play-configuration-dialog/android-google-play-configuration-dialog.component';
import { ProductBundleListComponent } from './digital-goods/product-bundle-list/product-bundle-list.component';
import { ProductBundleEditorComponent } from './digital-goods/product-bundle-editor/product-bundle-editor.component';
import { BundleRewardsEditorComponent } from './digital-goods/bundle-rewards-editor/bundle-rewards-editor.component';
import { ProfilesListComponent } from './profiles/profiles-list/profiles-list.component';
import { ProfileDialogComponent } from './profiles/profile-dialog/profile-dialog.component';
import { UserSelectDialogComponent } from './users/user-select-dialog/user-select-dialog.component';
import { LeaderboardsListComponent } from './leaderboards/leaderboards-list/leaderboards-list.component';
import { LeaderboardDialogComponent } from './leaderboards/leaderboard-dialog/leaderboard-dialog.component';
import { InventoryDialogComponent } from './digital-goods/inventory-dialog/inventory-dialog.component';
import { FungibleInventoryEditorComponent } from './digital-goods/inventory-dialog/fungible-inventory-editor/fungible-inventory-editor.component';
import { FungibleAddInventoryComponent } from './digital-goods/inventory-dialog/fungible-add-inventory/fungible-add-inventory.component';
import { FungibleModifyInventoryComponent } from './digital-goods/inventory-dialog/fungible-modify-inventory/fungible-modify-inventory.component';
import { DistinctAddInventoryComponent } from './digital-goods/inventory-dialog/distinct-add-inventory/distinct-add-inventory.component';
import { DistinctModifyInventoryComponent } from './digital-goods/inventory-dialog/distinct-modify-inventory/distinct-modify-inventory.component';
import { DistinctInventoryEditorComponent } from './digital-goods/inventory-dialog/distinct-inventory-editor/distinct-inventory-editor.component';
import { ItemSelectDialogComponent } from './digital-goods/inventory-dialog/item-select-dialog/item-select-dialog.component';
import { WalletsListComponent } from './omni-chain/wallets-list/wallets-list.component';
import { WalletDialogComponent } from './omni-chain/wallets-dialog/wallet-dialog.component';
import { NeoTokensListComponent } from './omni-chain/neo/neo-tokens-list/neo-tokens-list.component';
import { NeoTokenDialogComponent } from './omni-chain/neo/neo-token-dialog/neo-token-dialog.component';
import { YesNoPipe } from './omni-chain/neo/neo-tokens-list/yesNo.pipe';
import { TransferOptionsPipe } from './omni-chain/neo/neo-tokens-list/transferOptions.pipe';
import { TokensMenuComponent } from './omni-chain/tokens-menu/tokens-menu.component';
import { AuthSchemesComponent } from "./auth/auth-schemes.component";
import { CustomAuthSchemesListComponent } from './auth/custom/custom-auth-schemes-list/custom-auth-schemes-list.component';
import { CustomAuthSchemeDialogComponent } from './auth/custom/custom-auth-scheme-dialog/custom-auth-scheme-dialog.component';
import { OidcAuthSchemesListComponent } from './auth/oidc/oidc-auth-schemes-list/oidc-auth-schemes-list.component';
import { OidcAuthSchemeDialogComponent } from './auth/oidc/oidc-auth-scheme-dialog/oidc-auth-scheme-dialog.component';
import { Oauth2AuthSchemesListComponent } from './auth/oauth2/oauth2-auth-schemes-list/oauth2-auth-schemes-list.component';
import { Oauth2AuthSchemeDialogComponent } from './auth/oauth2/oauth2-auth-scheme-dialog/oauth2-auth-scheme-dialog.component';
import { RegenerateKeysDialogComponent } from './auth/keygen/regenerate-keys-dialog/regenerate-keys-dialog.component';
import { GeneratedKeysDialogComponent } from './auth/keygen/generated-keys-dialog/generated-keys-dialog.component';
import { NeoSmartContractsListComponent } from './omni-chain/neo/neo-smart-contracts-list/neo-smart-contracts-list.component';
import { NeoSmartContractsDialogComponent } from './omni-chain/neo/neo-smart-contracts-dialog/neo-smart-contracts-dialog.component';
import { NeoSmartContractSelectDialogComponent } from './omni-chain/neo/neo-smart-contract-select-dialog/neo-smart-contract-select-dialog.component';
import { NeoSmartContractMintDialogComponent } from './omni-chain/neo/neo-smart-contract-mint-dialog/neo-smart-contract-mint-dialog.component';
import { WalletSelectDialogComponent } from './omni-chain/wallet-select-dialog/wallet-select-dialog.component';
import { PercentageDirective } from './omni-chain/neo/neo-token-dialog/percentage-directive.directive';
import { BlockchainDropdownComponent } from './omni-chain/blockchain-dropdown/blockchain-dropdown.component';
import { TokenViewerDialogComponent } from './omni-chain/token-viewer-dialog/token-viewer-dialog.component';
import { TokenViewLightboxDialogComponent } from './omni-chain/token-view-lightbox-dialog/token-view-lightbox-dialog.component';
import { NeoSmartTokenSpecsDialogComponent } from './omni-chain/neo/neo-smart-token-specs-dialog/neo-smart-token-specs-dialog.component';
import { NeoSmartTokenSpecsDialogFieldTypeComponent } from './omni-chain/neo/neo-smart-token-specs-dialog/neo-smart-token-specs-dialog-field-type/neo-smart-token-specs-dialog-field-type.component';
import { NeoTokenDialogUpdatedComponent } from './omni-chain/neo/neo-token-dialog-updated/neo-token-dialog-updated.component';
import { NeoTokenDialogDefineObjectComponent } from './omni-chain/neo/neo-token-dialog-define-object/neo-token-dialog-define-object.component';
import { NeoSmartTokenSpecsMoveFieldDialogComponent } from './omni-chain/neo/neo-smart-token-specs-move-field-dialog/neo-smart-token-specs-move-field-dialog.component';
import { CustomizationMenuComponent } from './customization/customization-menu/customization-menu.component';
import { NeoTokenDialogUpdatedFieldComponent } from './omni-chain/neo/neo-token-dialog-updated-field/neo-token-dialog-updated-field.component';
import { NeoTokenDialogUpdatedDefineComponent } from './omni-chain/neo/neo-token-dialog-updated-define/neo-token-dialog-updated-define.component';
import { NeoSmartTokenSpecsDuplicateDialogComponent } from './omni-chain/neo/neo-smart-token-specs-duplicate-dialog/neo-smart-token-specs-duplicate-dialog.component';
import { TokenDefinationDuplicateDialogComponent } from './omni-chain/token-defination-duplicate-dialog/token-defination-duplicate-dialog.component';
import { NeoSmartTokenSpecsComponent } from './omni-chain/neo/neo-smart-token-specs/neo-smart-token-specs/neo-smart-token-specs.component';
import { MetadataSpecsComponent } from "./customization/metadata-spec/menu/metadata-specs.component";
import { MetadataSpecsDialogComponent } from "./customization/metadata-spec/menu/metadata-specs-dialog/metadata-specs-dialog.component";
import { MetadataSpecsDuplicateDialogComponent } from "./customization/metadata-spec/menu/metadata-specs-duplicate-dialog/metadata-specs-duplicate-dialog.component";
import { OmniChainComponent } from './omni-chain/omni-chain.component';
import { OmniChainVaultsComponent } from './omni-chain/omni-chain-vaults/omni-chain-vaults.component';
import { OmniChainVaultsDialogComponent } from './omni-chain/omni-chain-vaults-dialog/omni-chain-vaults-dialog.component';
import { OmniChainVaultsWalletsDialogComponent } from './omni-chain/omni-chain-vaults-wallets-dialog/omni-chain-vaults-wallets-dialog.component';
import { OmniChainWalletsComponent } from './omni-chain/omni-chain-wallets/omni-chain-wallets.component';
import { OmniChainContractsComponent } from './omni-chain/omni-chain-contracts/omni-chain-contracts.component';
import { OmniChainContractsDialogComponent } from './omni-chain/omni-chain-contracts-dialog/omni-chain-contracts-dialog.component';
import { OmniChainWalletsDialogComponent } from './omni-chain/omni-chain-wallets-dialog/omni-chain-wallets-dialog.component';
import { OmniChainWalletsVaultSearchDialogComponent } from './omni-chain/omni-chain-wallets-vault-search-dialog/omni-chain-wallets-vault-search-dialog.component';
import { OmniChainUserSearchDialogComponent } from './omni-chain/omni-chain-user-search-dialog/omni-chain-user-search-dialog.component';
import { OmniChainWalletsAccountsDialogComponent } from './omni-chain/omni-chain-wallets-accounts-dialog/omni-chain-wallets-accounts-dialog.component';
import { ApplicationAttributesComponent } from './applications/application-attributes/application-attributes.component';
import {MetadataspecSelectDialogComponent} from "./customization/metadata-spec/metadataspec-select-dialog/metadataspec-select-dialog.component";
import {SchedulesListComponent} from "./schedules/schedules-list/schedules-list.component";
import {ScheduleDialogComponent} from "./schedules/schedule-dialog/schedule-dialog.component";
import {ScheduleEventsDialogComponent} from "./schedules/schedule-events-dialog/schedule-events-dialog.component";
import {
  ScheduleEventMissionsDialogComponent
} from "./schedules/schedule-event-missions-dialog/schedule-event-missions-dialog.component";
import {
  MissionSelectDialogComponent
} from "./schedules/schedule-event-missions-dialog/mission-select-dialog/mission-select-dialog.component";
import { LastSegmentCapitalizePipe } from './users/last-segment-capitalize.pipe';
import {
  MetadataSpecsDialogFieldTypeComponent
} from "./customization/metadata-spec/menu/metadata-specs-dialog/metadata-specs-dialog-field-type/metadata-specs-dialog-field-type.component";
import {
  MetadataSpecDialogDefineObjectComponent
} from "./customization/metadata-spec/menu/metadata-specs-dialog/metadata-spec-dialog-define-object/metadata-spec-dialog-define-object.component";

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
    ScheduleDialogComponent,
    ScheduleEventsDialogComponent,
    ScheduleEventMissionsDialogComponent,
    FacebookApplicationConfigurationDialogComponent,
    FirebaseApplicationConfigurationDialogComponent,
    MatchmakingApplicationConfigurationDialogComponent,
    IosApplicationConfigurationDialogComponent,
    AndroidGooglePlayConfigurationDialogComponent,
    ProductBundleEditorComponent,
    ProfileDialogComponent,
    UserSelectDialogComponent,
    MetadataspecSelectDialogComponent,
    LeaderboardDialogComponent,
    InventoryDialogComponent,
    ItemSelectDialogComponent
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
    ApplicationAttributesComponent,
    ItemDialogComponent,
    UserDialogComponent,
    ApplicationConfigurationsListComponent,
    FacebookApplicationConfigurationDialogComponent,
    FirebaseApplicationConfigurationDialogComponent,
    MatchmakingApplicationConfigurationDialogComponent,
    ItemsListComponent,
    ItemDialogComponent,
    SimpleJsonEditorComponent,
    JsonEditorCardComponent,
    MissionsListComponent,
    SchedulesListComponent,
    MissionDialogComponent,
    ScheduleDialogComponent,
    ScheduleEventsDialogComponent,
    ScheduleEventMissionsDialogComponent,
    MissionSelectDialogComponent,
    MissionStepsCardComponent,
    MissionRewardsEditorComponent,
    IosApplicationConfigurationDialogComponent,
    AndroidGooglePlayConfigurationDialogComponent,
    ProductBundleListComponent,
    ProductBundleEditorComponent,
    BundleRewardsEditorComponent,
    ProfilesListComponent,
    ProfileDialogComponent,
    UserSelectDialogComponent,
    MetadataspecSelectDialogComponent,
    LeaderboardsListComponent,
    LeaderboardDialogComponent,
    WalletsListComponent,
    WalletDialogComponent,
    InventoryDialogComponent,
    FungibleAddInventoryComponent,
    FungibleModifyInventoryComponent,
    FungibleInventoryEditorComponent,
    DistinctAddInventoryComponent,
    DistinctModifyInventoryComponent,
    DistinctInventoryEditorComponent,
    ItemSelectDialogComponent,
    NeoTokensListComponent,
    NeoTokenDialogComponent,
    YesNoPipe,
    TransferOptionsPipe,
    TokensMenuComponent,
    AuthSchemesComponent,
    CustomAuthSchemesListComponent,
    CustomAuthSchemeDialogComponent,
    OidcAuthSchemesListComponent,
    OidcAuthSchemeDialogComponent,
    Oauth2AuthSchemesListComponent,
    Oauth2AuthSchemeDialogComponent,
    RegenerateKeysDialogComponent,
    GeneratedKeysDialogComponent,
    MetadataSpecsComponent,
    MetadataSpecsDialogComponent,
    MetadataSpecsDuplicateDialogComponent,
    MetadataSpecsDialogFieldTypeComponent,
    MetadataSpecDialogDefineObjectComponent,
    NeoSmartContractsListComponent,
    NeoSmartContractsDialogComponent,
    NeoSmartContractSelectDialogComponent,
    NeoSmartContractMintDialogComponent,
    WalletSelectDialogComponent,
    PercentageDirective,
    BlockchainDropdownComponent,
    TokenViewerDialogComponent,
    TokenViewLightboxDialogComponent,
    NeoSmartTokenSpecsDialogComponent,
    NeoSmartTokenSpecsDialogFieldTypeComponent,
    NeoTokenDialogUpdatedComponent,
    NeoTokenDialogDefineObjectComponent,
    NeoSmartTokenSpecsMoveFieldDialogComponent,
    CustomizationMenuComponent,
    NeoTokenDialogUpdatedFieldComponent,
    NeoTokenDialogUpdatedDefineComponent,
    NeoSmartTokenSpecsDuplicateDialogComponent,
    TokenDefinationDuplicateDialogComponent,
    NeoSmartTokenSpecsComponent,
    OmniChainComponent,
    OmniChainVaultsComponent,
    OmniChainVaultsDialogComponent,
    OmniChainVaultsWalletsDialogComponent,
    OmniChainWalletsComponent,
    OmniChainContractsComponent,
    OmniChainContractsDialogComponent,
    OmniChainWalletsDialogComponent,
    OmniChainWalletsVaultSearchDialogComponent,
    OmniChainUserSearchDialogComponent,
    OmniChainWalletsAccountsDialogComponent,
    LastSegmentCapitalizePipe
  ],
  imports: [
    BrowserModule,
    MatDialogModule,
    MatFormFieldModule,
    FormsModule,
    BrowserAnimationsModule,
    HttpClientModule,
    LayoutModule,
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
    { provide: HTTP_INTERCEPTORS, useClass: ApiErrorInterceptor, multi: true }
  ],
  exports: [
    LastSegmentCapitalizePipe
  ],
  bootstrap: [AppComponent]
})

export class AppModule { }
