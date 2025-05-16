package dev.getelements.elements.dao.mongo.mapper;

import dev.getelements.elements.sdk.model.leaderboard.Rank;
import dev.getelements.elements.sdk.model.leaderboard.RankRow;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {PropertyConverters.class, MongoApplicationMapper.class, MongoDBMapper.class})
public interface RankTabulationMapper extends MapperRegistry.Mapper<Rank, RankRow> {

    @Override
    @Mapping(target = "id", source = "score.id")
    @Mapping(target = "pointValue", source = "score.pointValue")
    @Mapping(target = "scoreUnits", source = "score.scoreUnits")
    @Mapping(target = "creationTimestamp", source = "score.creationTimestamp")
    @Mapping(target = "leaderboardEpoch", source = "score.leaderboardEpoch")
    @Mapping(target = "profileId", source = "score.profile.id")
    @Mapping(target = "profileDisplayName", source = "score.profile.displayName")
    @Mapping(target = "profileImageUrl", source = "score.profile.imageUrl")
    @Mapping(target = "lastLogin", source = "score.profile.lastLogin")
    RankRow forward(Rank source);

}
