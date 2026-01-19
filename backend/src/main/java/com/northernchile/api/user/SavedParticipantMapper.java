package com.northernchile.api.user;

import com.northernchile.api.model.SavedParticipant;
import com.northernchile.api.user.dto.SavedParticipantRes;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SavedParticipantMapper {

    @Mapping(target = "isSelf", source = "self")
    SavedParticipantRes toRes(SavedParticipant entity);

    List<SavedParticipantRes> toResList(List<SavedParticipant> entities);
}
