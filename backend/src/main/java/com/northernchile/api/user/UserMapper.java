package com.northernchile.api.user;

import com.northernchile.api.model.User;
import com.northernchile.api.user.dto.UserRes;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserRes toUserRes(User user);

    List<UserRes> toUserResList(List<User> users);
}
