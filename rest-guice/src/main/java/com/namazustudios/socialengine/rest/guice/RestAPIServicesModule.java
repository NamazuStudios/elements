package com.namazustudios.socialengine.rest.guice;

import com.google.inject.PrivateModule;
import com.google.inject.Scope;
import com.google.inject.servlet.ServletScopes;
import com.namazustudios.socialengine.rt.Attributes;
import com.namazustudios.socialengine.security.PasswordGenerator;
import com.namazustudios.socialengine.security.SecureRandomPasswordGenerator;
import com.namazustudios.socialengine.service.*;
import com.namazustudios.socialengine.service.appleiap.AppleIapReceiptService;
import com.namazustudios.socialengine.service.appleiap.AppleIapReceiptServiceProvider;
import com.namazustudios.socialengine.service.application.*;
import com.namazustudios.socialengine.service.auth.*;
import com.namazustudios.socialengine.service.friend.FacebookFriendServiceProvider;
import com.namazustudios.socialengine.service.gameon.*;
import com.namazustudios.socialengine.service.goods.ItemServiceProvider;
import com.namazustudios.socialengine.service.googleplayiap.GooglePlayIapReceiptService;
import com.namazustudios.socialengine.service.googleplayiap.GooglePlayIapReceiptServiceProvider;
import com.namazustudios.socialengine.service.guice.ServicesModule;
import com.namazustudios.socialengine.service.inventory.SimpleInventoryItemService;
import com.namazustudios.socialengine.service.inventory.SimpleInventoryItemServiceProvider;
import com.namazustudios.socialengine.service.leaderboard.LeaderboardServiceProvider;
import com.namazustudios.socialengine.service.leaderboard.RankServiceProvider;
import com.namazustudios.socialengine.service.leaderboard.ScoreServiceProvider;
import com.namazustudios.socialengine.service.friend.FriendServiceProvider;
import com.namazustudios.socialengine.service.manifest.ManifestServiceProvider;
import com.namazustudios.socialengine.service.match.MatchServiceProvider;
import com.namazustudios.socialengine.service.match.StandardMatchServiceUtils;
import com.namazustudios.socialengine.service.mission.*;
import com.namazustudios.socialengine.service.notification.FCMRegistrationServiceProvider;
import com.namazustudios.socialengine.service.profile.ProfileOverrideServiceProvider;
import com.namazustudios.socialengine.service.profile.ProfileServiceProvider;
import com.namazustudios.socialengine.service.progress.ProgressService;
import com.namazustudios.socialengine.service.progress.ProgressServiceProvider;
import com.namazustudios.socialengine.service.rewardissuance.RewardIssuanceService;
import com.namazustudios.socialengine.service.rewardissuance.RewardIssuanceServiceProvider;
import com.namazustudios.socialengine.service.shortlink.ShortLinkServiceProvider;
import com.namazustudios.socialengine.service.social.SocialCampaignServiceProvider;
import com.namazustudios.socialengine.service.user.UserServiceProvider;
import com.namazustudios.socialengine.util.DisplayNameGenerator;
import com.namazustudios.socialengine.util.SimpleDisplayNameGenerator;
import org.dozer.Mapper;

import javax.inject.Provider;

/**
 * Created by patricktwohig on 3/19/15.
 */
public class RestAPIServicesModule extends ServicesModule {

    public RestAPIServicesModule() {
        super(ServletScopes.REQUEST, AttributesProvider.class);
    }

}
