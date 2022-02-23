/* tslint:disable */
import { NgModule } from '@angular/core';
import { HttpClientModule } from '@angular/common/http';
import { ApiConfiguration } from './api-configuration';

import { EntrantsService } from './services/entrants.service';
import { GooglePlayApplicationConfigurationService } from './services/google-play-application-configuration.service';
import { MatchesService } from './services/matches.service';
import { ProfilesService } from './services/profiles.service';
import { ShortLinksService } from './services/short-links.service';
import { UsersService } from './services/users.service';
import { SeverVersionMetadataService } from './services/sever-version-metadata.service';
import { ApplicationConfigurationsService } from './services/application-configurations.service';
import { ApplicationsService } from './services/applications.service';
import { FacebookApplicationConfigurationService } from './services/facebook-application-configuration.service';
import { FirebaseApplicationConfigurationService } from './services/firebase-application-configuration.service';
import { IOSApplicationConfigurationService } from './services/iosapplication-configuration.service';
import { MatchmakingApplicationConfigurationService } from './services/matchmaking-application-configuration.service';
import { PSNApplicationConfigurationsService } from './services/psnapplication-configurations.service';
import { FriendsService } from './services/friends.service';
import { GameOnPrizesService } from './services/game-on-prizes.service';
import { GameOnMatchesService } from './services/game-on-matches.service';
import { GameOnPlayerTournamentService } from './services/game-on-player-tournament.service';
import { GameOnRegistrationService } from './services/game-on-registration.service';
import { GameOnSessionService } from './services/game-on-session.service';
import { GameOnEntryService } from './services/game-on-entry.service';
import { GameOnTournamentService } from './services/game-on-tournament.service';
import { ItemsService } from './services/items.service';
import { LeaderboardsService } from './services/leaderboards.service';
import { RankingService } from './services/ranking.service';
import { ScoresService } from './services/scores.service';
import { FirebaseCloudNotificationsService } from './services/firebase-cloud-notifications.service';
import { FacebookSessionService } from './services/facebook-session.service';
import { MockSessionsService } from './services/mock-sessions.service';
import { SessionAndLoginService } from './services/session-and-login.service';
import { UsernamePasswordSessionService } from './services/username-password-session.service';
import { MissionsService } from './services/missions.service';
import { FungibleInventoryService } from './services/fungible-inventory.service';
import { NeoWalletsService } from './services/blockchain/neo-wallets.service';

/**
 * Provider for all Api services, plus ApiConfiguration
 */
@NgModule({
  imports: [
    HttpClientModule
  ],
  exports: [
    HttpClientModule
  ],
  declarations: [],
  providers: [
    ApiConfiguration,
    EntrantsService,
    GooglePlayApplicationConfigurationService,
    MatchesService,
    ProfilesService,
    ShortLinksService,
    UsersService,
    SeverVersionMetadataService,
    ApplicationConfigurationsService,
    ApplicationsService,
    FacebookApplicationConfigurationService,
    FirebaseApplicationConfigurationService,
    IOSApplicationConfigurationService,
    MatchmakingApplicationConfigurationService,
    PSNApplicationConfigurationsService,
    FriendsService,
    GameOnPrizesService,
    GameOnMatchesService,
    GameOnPlayerTournamentService,
    GameOnRegistrationService,
    GameOnSessionService,
    GameOnEntryService,
    GameOnTournamentService,
    ItemsService,
    LeaderboardsService,
    NeoWalletsService,
    RankingService,
    ScoresService,
    FirebaseCloudNotificationsService,
    FacebookSessionService,
    MockSessionsService,
    SessionAndLoginService,
    UsernamePasswordSessionService,
    MissionsService,
    FungibleInventoryService
  ],
})
export class ApiModule { }
