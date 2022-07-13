import { Routes, RouterModule } from "@angular/router";

import { LoginComponent } from "./login/login.component";
import { HomeComponent } from "./home/home.component";
import { UsersListComponent } from "./users-list/users-list.component";
import { ApplicationsListComponent } from "./applications-list/applications-list.component";
import { AuthenticationGuard } from "./authentication.guard";
import { ItemsListComponent } from "./items-list/items-list.component";
import { MissionsListComponent } from "./missions-list/missions-list.component";
import { ProfilesListComponent } from "./profiles-list/profiles-list.component";
import { LeaderboardsListComponent } from "./leaderboards-list/leaderboards-list.component";
import { TokensMenuComponent } from "./tokens-menu/tokens-menu.component";
import { AuthSchemesListComponent } from "./auth-schemes-list/auth-schemes-list.component";
import { NeoSmartContractsListComponent } from "./neo-smart-contracts-list/neo-smart-contracts-list.component";
import { CustomizationMenuComponent } from "./customization-menu/customization-menu.component";

const appRoutes: Routes = [
  { path: "login", component: LoginComponent },
  { path: "", component: HomeComponent, canActivate: [AuthenticationGuard] },
  {
    path: "users",
    component: UsersListComponent,
    canActivate: [AuthenticationGuard],
  },
  // { path: 'users/:id', component: UserDetailComponent, canActivate: [AuthenticationGuard] },
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
    path: "tokens",
    component: TokensMenuComponent,
    canActivate: [AuthenticationGuard],
  },
  {
    path: "auth-schemes",
    component: AuthSchemesListComponent,
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
  // { path: 'applications/:id', component: ApplicationDetailComponent, canActivate: [AuthenticationGuard] },
  // { path: 'applications/:applicationId/configurations/:id', component: ApplicationConfigurationDetailComponent, canActivate: [AuthenticationGuard] },
  { path: "**", redirectTo: "" },
];

export const routing = RouterModule.forRoot(appRoutes, {
  relativeLinkResolution: "legacy",
});
