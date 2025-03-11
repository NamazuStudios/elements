import { Routes, RouterModule } from "@angular/router";

import { LoginComponent } from "./login/login.component";
import { HomeComponent } from "./home/home.component";
import { UsersListComponent } from "./users/users-list/users-list.component";
import { ApplicationsListComponent } from "./applications/applications-list/applications-list.component";
import { AuthenticationGuard } from "./login/authentication.guard";
import { ItemsListComponent } from "./digital-goods/items-list/items-list.component";
import { MissionsListComponent } from "./missions/missions-list/missions-list.component";
import { ProfilesListComponent } from "./profiles/profiles-list/profiles-list.component";
import { LeaderboardsListComponent } from "./leaderboards/leaderboards-list/leaderboards-list.component";
import { TokensMenuComponent } from "./omni-chain/tokens-menu/tokens-menu.component";
import { CustomAuthSchemesListComponent } from "./auth/custom/custom-auth-schemes-list/custom-auth-schemes-list.component";
import { NeoSmartContractsListComponent } from "./omni-chain/neo/neo-smart-contracts-list/neo-smart-contracts-list.component";
import { CustomizationMenuComponent } from "./customization/customization-menu/customization-menu.component";
import { OmniChainComponent } from "./omni-chain/omni-chain.component";
import {SchedulesListComponent} from "./schedules/schedules-list/schedules-list.component";
import {AuthSchemesComponent} from "./auth/auth-schemes.component";

const appRoutes: Routes = [
  { path: "login", component: LoginComponent },
  { path: "", component: HomeComponent, canActivate: [AuthenticationGuard] },
  {
    path: "users",
    component: UsersListComponent,
    canActivate: [AuthenticationGuard],
  },
  {
    path: "applications",
    component: ApplicationsListComponent,
    canActivate: [AuthenticationGuard],
  },
  {
    path: "digital-goods",
    component: ItemsListComponent,
    canActivate: [AuthenticationGuard],
  },
  {
    path: "missions",
    component: MissionsListComponent,
    canActivate: [AuthenticationGuard],
  },
  {
    path: "schedules",
    component: SchedulesListComponent,
    canActivate: [AuthenticationGuard],
  },
  {
    path: "profiles",
    component: ProfilesListComponent,
    canActivate: [AuthenticationGuard],
  },
  {
    path: "leaderboards",
    component: LeaderboardsListComponent,
    canActivate: [AuthenticationGuard],
  },
  {
    path: "omni-chain",
    component: OmniChainComponent,
    canActivate: [AuthenticationGuard],
  },
  {
    path: "tokens",
    component: TokensMenuComponent,
    canActivate: [AuthenticationGuard],
  },
  {
    path: "auth-schemes",
    component: AuthSchemesComponent,
    canActivate: [AuthenticationGuard],
  },
  {
    path: "customization",
    component: CustomizationMenuComponent,
    canActivate: [AuthenticationGuard],
  },
  {
    path: "smart-contracts",
    component: NeoSmartContractsListComponent,
    canActivate: [AuthenticationGuard],
  },
  { path: "**", redirectTo: "" },
];

export const routing = RouterModule.forRoot(appRoutes, {
  relativeLinkResolution: "legacy",
});
