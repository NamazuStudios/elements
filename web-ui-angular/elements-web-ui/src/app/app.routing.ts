import { Routes, RouterModule } from '@angular/router';

import { LoginComponent } from "./login/login.component";
import { HomeComponent } from "./home/home.component";
import { UsersListComponent } from "./users-list/users-list.component";
import { ApplicationsListComponent } from "./applications-list/applications-list.component";
import { AuthenticationGuard } from './authentication.guard';
import { ItemsListComponent } from './items-list/items-list.component';
import { MissionsListComponent} from './missions-list/missions-list.component';
import { ProfilesListComponent } from './profiles-list/profiles-list.component';

const appRoutes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: '', component: HomeComponent, canActivate: [AuthenticationGuard] },
  { path: 'users', component: UsersListComponent, canActivate: [AuthenticationGuard] },
  // { path: 'users/:id', component: UserDetailComponent, canActivate: [AuthenticationGuard] },
  { path: 'applications', component: ApplicationsListComponent, canActivate: [AuthenticationGuard] },
  { path: 'digital-goods', component: ItemsListComponent, canActivate: [AuthenticationGuard] },
  { path: 'missions', component: MissionsListComponent, canActivate: [AuthenticationGuard] },
  { path: 'profiles', component: ProfilesListComponent, canActivate: [AuthenticationGuard] },
  // { path: 'applications/:id', component: ApplicationDetailComponent, canActivate: [AuthenticationGuard] },
  // { path: 'applications/:applicationId/configurations/:id', component: ApplicationConfigurationDetailComponent, canActivate: [AuthenticationGuard] },
  { path: '**', redirectTo: '' }
];

export const routing = RouterModule.forRoot(appRoutes, {
//  enableTracing: true
});
