import { BrowserModule } from '@angular/platform-browser';
import { APP_INITIALIZER, NgModule} from '@angular/core';
import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { LayoutModule } from '@angular/cdk/layout';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import {MatDialogModule} from '@angular/material/dialog';
import {MatFormFieldModule} from '@angular/material/form-field';

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

import { AuthenticationService } from './authentication.service';
import { AlertService } from './alert.service';
import {ApiErrorInterceptor} from './api-error.interceptor';
import {MaterialModule} from './material/material.module';
import {ConfirmationDialogService} from './confirmation-dialog/confirmation-dialog.service';
import {ApplicationDialogComponent} from './application-dialog/application-dialog.component';
import { UserDialogComponent } from './user-dialog/user-dialog.component';
import { ApplicationConfigurationsListComponent } from './application-configurations-list/application-configurations-list.component';
import { FacebookApplicationConfigurationDialogComponent } from './facebook-application-configuration-dialog/facebook-application-configuration-dialog.component';
import { FirebaseApplicationConfigurationDialogComponent } from './firebase-application-configuration-dialog/firebase-application-configuration-dialog.component';
import {ConfigService} from './config.service';
import { MatchmakingApplicationConfigurationDialogComponent } from './matchmaking-application-configuration-dialog/matchmaking-application-configuration-dialog.component';
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
import { BundleRewardsEditorComponent } from './bundle-rewards-editor/bundle-rewards-editor.component';
import { ProfilesListComponent } from './profiles-list/profiles-list.component';
import { ProfileDialogComponent } from './profile-dialog/profile-dialog.component';
import { UserSelectDialogComponent } from './user-select-dialog/user-select-dialog.component';
import { LeaderboardsListComponent } from './leaderboards-list/leaderboards-list.component';
import { LeaderboardDialogComponent } from './leaderboard-dialog/leaderboard-dialog.component';
import { InventoryDialogComponent } from './inventory-dialog/inventory-dialog.component';
import { FungibleInventoryEditorComponent } from './inventory-dialog/fungible-inventory-editor/fungible-inventory-editor.component';
import { FungibleAddInventoryComponent } from './inventory-dialog/fungible-add-inventory/fungible-add-inventory.component';
import { FungibleModifyInventoryComponent } from './inventory-dialog/fungible-modify-inventory/fungible-modify-inventory.component';
import { DistinctAddInventoryComponent } from './inventory-dialog/distinct-add-inventory/distinct-add-inventory.component';
import { DistinctModifyInventoryComponent } from './inventory-dialog/distinct-modify-inventory/distinct-modify-inventory.component';
import { DistinctInventoryEditorComponent } from './inventory-dialog/distinct-inventory-editor/distinct-inventory-editor.component';
import { ItemSelectDialogComponent } from './inventory-dialog/item-select-dialog/item-select-dialog.component';
import { WalletsListComponent } from './wallets-list/wallets-list.component';
import { WalletDialogComponent } from './wallets-dialog/wallet-dialog.component';
import { NeoTokensListComponent } from './neo-tokens-list/neo-tokens-list.component';
import { NeoTokenDialogComponent } from './neo-token-dialog/neo-token-dialog.component';
import { YesNoPipe } from './neo-tokens-list/yesNo.pipe';
import { TransferOptionsPipe } from './neo-tokens-list/transferOptions.pipe';
import { TokensMenuComponent } from './tokens-menu/tokens-menu.component';
import { AuthSchemesListComponent } from './auth-schemes-list/auth-schemes-list.component';
import { AuthSchemeDialogComponent } from './auth-scheme-dialog/auth-scheme-dialog.component';
import { RegenerateKeysDialogComponent } from './auth-scheme-dialog/regenerate-keys-dialog/regenerate-keys-dialog.component';
import { GeneratedKeysDialogComponent } from './auth-scheme-dialog/generated-keys-dialog/generated-keys-dialog.component';
import { NeoSmartContractsListComponent } from './neo-smart-contracts-list/neo-smart-contracts-list.component';
import { NeoSmartContractsDialogComponent } from './neo-smart-contracts-dialog/neo-smart-contracts-dialog.component';
import { NeoSmartContractSelectDialogComponent } from './neo-smart-contract-select-dialog/neo-smart-contract-select-dialog.component';
import { NeoSmartContractMintDialogComponent } from './neo-smart-contract-mint-dialog/neo-smart-contract-mint-dialog.component';
import { WalletSelectDialogComponent } from './wallet-select-dialog/wallet-select-dialog.component';
import { PercentageDirective } from './neo-token-dialog/percentage-directive.directive';
import { BlockchainDropdownComponent } from './blockchain-dropdown/blockchain-dropdown.component';
import { TokenViewerDialogComponent } from './token-viewer-dialog/token-viewer-dialog.component';
import { TokenViewLightboxDialogComponent } from './token-view-lightbox-dialog/token-view-lightbox-dialog.component';
import { NeoSmartTokenSpecsDialogComponent } from './neo-smart-token-specs-dialog/neo-smart-token-specs-dialog.component';
import { NeoSmartTokenSpecsDialogFieldTypeComponent } from './neo-smart-token-specs-dialog/neo-smart-token-specs-dialog-field-type/neo-smart-token-specs-dialog-field-type.component';
import { NeoTokenDialogUpdatedComponent } from './neo-token-dialog-updated/neo-token-dialog-updated.component';
import { NeoTokenDialogDefineObjectComponent } from './neo-token-dialog-define-object/neo-token-dialog-define-object.component';
import { NeoSmartTokenSpecsMoveFieldDialogComponent } from './neo-smart-token-specs-move-field-dialog/neo-smart-token-specs-move-field-dialog.component';
import { CustomizationMenuComponent } from './customization-menu/customization-menu.component';
import { NeoTokenDialogUpdatedFieldComponent } from './neo-token-dialog-updated-field/neo-token-dialog-updated-field.component';
import { NeoTokenDialogUpdatedDefineComponent } from './neo-token-dialog-updated-define/neo-token-dialog-updated-define.component';
import { NeoSmartTokenSpecsDuplicateDialogComponent } from './neo-smart-token-specs-duplicate-dialog/neo-smart-token-specs-duplicate-dialog.component';
import { TokenDefinationDuplicateDialogComponent } from './token-defination-duplicate-dialog/token-defination-duplicate-dialog.component';
import { NeoSmartTokenSpecsComponent } from './neo-smart-token-specs/neo-smart-token-specs/neo-smart-token-specs.component';
import { OmniChainComponent } from './omni-chain/omni-chain.component';
import { OmniChainVaultsComponent } from './omni-chain-vaults/omni-chain-vaults.component';
import { OmniChainVaultsDialogComponent } from './omni-chain-vaults-dialog/omni-chain-vaults-dialog.component';
import { OmniChainVaultsWalletsDialogComponent } from './omni-chain-vaults-wallets-dialog/omni-chain-vaults-wallets-dialog.component';
import { OmniChainWalletsComponent } from './omni-chain-wallets/omni-chain-wallets.component';
import { OmniChainContractsComponent } from './omni-chain-contracts/omni-chain-contracts.component';
import { OmniChainContractsDialogComponent } from './omni-chain-contracts-dialog/omni-chain-contracts-dialog.component';
import { OmniChainWalletsDialogComponent } from './omni-chain-wallets-dialog/omni-chain-wallets-dialog.component';
import { OmniChainWalletsVaultSearchDialogComponent } from './omni-chain-wallets-vault-search-dialog/omni-chain-wallets-vault-search-dialog.component';
import { OmniChainUserSearchDialogComponent } from './omni-chain-user-search-dialog/omni-chain-user-search-dialog.component';
import { OmniChainWalletsAccountsDialogComponent } from './omni-chain-wallets-accounts-dialog/omni-chain-wallets-accounts-dialog.component';
import { ApplicationAttributesComponent } from './application-attributes/application-attributes.component';
import {MetadataspecSelectDialogComponent} from "./metadataspec-select-dialog/metadataspec-select-dialog.component";
import {SchedulesListComponent} from "./schedules-list/schedules-list.component";
import {ScheduleDialogComponent} from "./schedule-dialog/schedule-dialog.component";

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
    AuthSchemesListComponent,
    AuthSchemeDialogComponent,
    RegenerateKeysDialogComponent,
    GeneratedKeysDialogComponent,
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
    OmniChainWalletsAccountsDialogComponent
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
  bootstrap: [AppComponent]
})

export class AppModule { }
